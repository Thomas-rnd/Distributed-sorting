package com.cs434.sortnet.master

import java.io._
import java.util.concurrent.{ExecutorService, Executors}
import java.net.{ServerSocket, Socket}
import scala.collection.mutable.{Map, HashMap, ListBuffer}
import scala.concurrent.{Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration._

import com.cs434.sortnet.network._
import com.cs434.sortnet.core._

import org.apache.logging.log4j.scala.Logging

object MasterServices extends Logging{

  def handleRegisterRequest(
    clientSocket: Socket,
    registerRequest: RegisterRequest,
    threadPool: ExecutorService,
    workerMetadataMap: Map[String, WorkerMetadata],
    numWorkers: Int
  ): Unit = {
    if (workerMetadataMap.size < numWorkers) {
      val clientIP = clientSocket.getInetAddress.getHostAddress
      logger.info(s"IP registered : $clientIP")

      // Create a WorkerMetadata with clientIP and the associated socket
      val workerMetadata = WorkerMetadata(clientIP, 0, clientSocket, None)
      workerMetadataMap.put(clientIP, workerMetadata)

      val reply = new RegisterReply(true)

      // Use the client's socket to send the RegisterReply
      try {
        val out = new ObjectOutputStream(clientSocket.getOutputStream)
        out.writeObject(reply)
      } catch {
        case e: IOException =>
          logger.error(s"IOException Error when registration : ${e.getMessage}", e)
          workerMetadataMap.remove(clientIP)
      }
    } else {
      val reply = new RegisterReply(false)
      try {
        val out = new ObjectOutputStream(clientSocket.getOutputStream)
        out.writeObject(reply)
      } catch {
        case e: IOException =>
          logger.error(s"IOException Error when deny registration : ${e.getMessage}", e)
      }
    }
  }


  def sendRequests(
      workerMetadataMap: Map[String, WorkerMetadata],
      messageType: MessageType.Value,
      partitionPlan: Option[PartitionPlan] = None,
      sampleKeys: Option[Map[String, List[Key]]] = None,
      success: Option[Boolean] = None,
      reason: Option[String] = None
  ): Unit = {
    val futures = workerMetadataMap.values.map { workerMetadata =>
      val promise = Promise[Boolean]()
      val future = promise.future

      val thread = new Thread(new Runnable {
        def run(): Unit = {
          try {
            val workerThreadMetadata = workerMetadata
            messageType match {
              case MessageType.SampleKey =>
                logger.info(s"Send SampleKey request to ${workerThreadMetadata.ip}")
                sendRequestThread(workerThreadMetadata, MessageType.SampleKey, sampleKeys = sampleKeys)
                promise.success(true)

              case MessageType.SavePartitionPlan =>
                logger.info(s"Send SavePartitionPlan request to ${workerThreadMetadata.ip}")
                sendRequestThread(workerThreadMetadata, MessageType.SavePartitionPlan, partitionPlan = partitionPlan)
                promise.success(true)

              case MessageType.Sort =>
                logger.info(s"Send sort request to ${workerThreadMetadata.ip}")
                sendRequestThread(workerThreadMetadata, MessageType.Sort)
                promise.success(true)

              case MessageType.Shuffle =>
                logger.info(s"Send shuffle request to ${workerThreadMetadata.ip}")
                sendRequestThread(workerThreadMetadata, MessageType.Shuffle)
                promise.success(true)

              case MessageType.Merge =>
                sendRequestThread(workerThreadMetadata, MessageType.Merge)
                promise.success(true)

              case MessageType.Terminate =>
                sendRequestThread(workerThreadMetadata, MessageType.Terminate, success = success , reason = reason)
                promise.success(true)

              case _ =>
                throw new RuntimeException(s"Unsupported message type: $messageType")
            } 
          } catch {
            case e: Throwable =>
              logger.error(s"${e.getMessage}")
              promise.failure(e)
          }
        }
      })

      thread.start()

      future
    }

    // Wait for all futures to complete before moving on
    Await.result(Future.sequence(futures), Duration.Inf)

    val aggregatedFuture = Future.sequence(futures)

    aggregatedFuture.onComplete {
      case scala.util.Success(results) =>
        logger.debug("All threads completed successfully")

        messageType match {
          case MessageType.SampleKey =>
            sampleKeys match {
              case Some(sampleKeysFound) =>
                logger.info("All samples received")
                for ((workerIP, keys) <- sampleKeysFound) {
                  logger.debug(s"Worker IP: $workerIP")
                  logger.debug(s"Sample Keys: $keys")
                }
                case None =>
                  throw new RuntimeException("sampleKeys not provided for SampleKey")
            }

          case _ => // Handle other message types
        }

      case scala.util.Failure(exception) =>
        logger.error(s"Error in one or more workers threads: ${exception.getMessage}")
        throw exception
    }
  }


  def sendRequestThread(
    workerMetadata: WorkerMetadata,
    messageType: MessageType.Value,
    partitionPlan: Option[PartitionPlan] = None,
    sampleKeys: Option[Map[String, List[Key]]] = None,
    success: Option[Boolean] = None,
    reason: Option[String] = None
  ): Unit = {
    try {
      val socket = workerMetadata.socket
      val out = new ObjectOutputStream(socket.getOutputStream)

      messageType match {
        case MessageType.SampleKey =>
          sampleKeys match {
            case Some(sampleKeysFound) =>
              val request = new SampleKeyRequest
              out.writeObject(request)

              // Wait for the SampleKeyReply
              val in = new ObjectInputStream(socket.getInputStream)
              val receivedObject = in.readObject
              if (receivedObject.isInstanceOf[SampleKeyReply]) {
                val reply = receivedObject.asInstanceOf[SampleKeyReply]
                if (reply.success) {
                  logger.debug(s"Worker ${workerMetadata.ip} succesfully sample Keys")
                  val keys = reply.sampledKeys.toList
                  logger.debug(s"Keys from worker ${workerMetadata.ip}: $keys")
                  sampleKeysFound.put(workerMetadata.ip, keys)
                } else {
                  throw new WorkerFailed(workerMetadata.ip, s"Worker ${workerMetadata.ip} failed to sample Keys")
                }
              }
            case None =>
              throw new RuntimeException("sampleKeys not provided for SampleKey")
          }

        case MessageType.SavePartitionPlan =>
          partitionPlan match {
            case Some(plan) =>
              val request = new SavePartitionPlanRequest(plan)
              out.writeObject(request)

              // Wait for the SavePartitionPlanReply
              val in = new ObjectInputStream(socket.getInputStream)
              val receivedObject = in.readObject
              if (receivedObject.isInstanceOf[SavePartitionPlanReply]) {
                val reply = receivedObject.asInstanceOf[SavePartitionPlanReply]
                if (reply.success) {
                  logger.debug(s"Worker ${workerMetadata.ip} succesfully save partitionPlan")
                } else {
                  throw new WorkerFailed(workerMetadata.ip, s"Worker failure : Worker ${workerMetadata.ip} failed to save partitionPlan")
                }
              }
            case None =>
              throw new RuntimeException("partitionPlan not provided for SavePartitionPlan")
          }

        case MessageType.Sort =>
          // Send the SortRequest
          val request = new SortRequest
          out.writeObject(request)

          // Wait for the SortReply
          val in = new ObjectInputStream(socket.getInputStream)
          val receivedObject = in.readObject
          if (receivedObject.isInstanceOf[SortReply]) {
            val reply = receivedObject.asInstanceOf[SortReply]
            if (reply.success) {
              logger.debug(s"Worker ${workerMetadata.ip} succesfully sort")
            } else {
              throw new WorkerFailed(workerMetadata.ip, s"Worker failure : Worker ${workerMetadata.ip} failed to sort")
            }
          }

        case MessageType.Shuffle =>
          // Send the ShuffleRequest
          val request = new ShuffleRequest
          out.writeObject(request)

          // Wait for the ShuffleReply
          val in = new ObjectInputStream(socket.getInputStream)
          val receivedObject = in.readObject
          if (receivedObject.isInstanceOf[ShuffleReply]) {
            val reply = receivedObject.asInstanceOf[ShuffleReply]
            val workerIP = workerMetadata.ip
            if (reply.success) {
              logger.debug(s"Worker ${workerMetadata.ip} succesfully shuffle")
            } else {
              throw new WorkerFailed(workerMetadata.ip, s"Worker failure : Worker ${workerMetadata.ip} failed to shuffle")
            }
          }

        case MessageType.Merge =>
          // Send the MergeRequest
          val request = new MergeRequest
          out.writeObject(request)

          // Wait for the MergeReply
          val in = new ObjectInputStream(socket.getInputStream)
          val receivedObject = in.readObject
          if (receivedObject.isInstanceOf[MergeReply]) {
            val reply = receivedObject.asInstanceOf[MergeReply]
            val workerIP = workerMetadata.ip
            if (reply.success) {
              logger.debug(s"Worker ${workerMetadata.ip} succesfully merge")
            } else {
              throw new WorkerFailed(workerMetadata.ip, s"Worker failure : Worker ${workerMetadata.ip} failed to merge")
            }
          }

        case MessageType.Terminate =>
          success match {
            case Some(suc) =>
              reason match {
                case Some(reas) =>
                  // Send the TerminateRequest
                  val request = new TerminateRequest(suc,reas)
                  out.writeObject(request)

                  // Wait for the TerminateReply
                  val in = new ObjectInputStream(socket.getInputStream)
                  
                  val receivedObject = in.readObject
                  if (receivedObject.isInstanceOf[TerminateReply]) {
                    val reply = receivedObject.asInstanceOf[TerminateReply]
                    val workerIP = workerMetadata.ip
                    if (reply.success) {
                      logger.debug(s"Worker ${workerMetadata.ip} succesfully terminate")
                    } else {
                      logger.error(s"Worker failure : Worker ${workerMetadata.ip} failed to terminate")
                    }
                  }
                case None =>
                  throw new RuntimeException("Reason of Terminate not provided for TerminateRequest")
              }
            case None =>
                throw new RuntimeException("success of Terminate not provided for TerminateRequest")
          }
          

        case _ =>
          throw new RuntimeException(s"Unsupported message type: $messageType")
      }
    } catch {
      case e: WorkerFailed =>
        throw e
      case e: IOException => // or java.net.SocketException or java.io.EOFException
        val workerIP = Option(workerMetadata).map(_.ip).getOrElse("Unknown IP")
        val errorMessage = s"Error in sending request to worker ${workerMetadata.ip}: ${e.getClass.getSimpleName}"
        val workerError = new WorkerError(workerIP, errorMessage)
        throw workerError
      case e: ClassNotFoundException =>
        throw e
      case e: RuntimeException =>
        throw e
      case e: Exception =>
        throw e
      case e: Throwable =>
        throw e
    }
  }

  /**
   * Finds pivot keys in a sorted list for partitioning.
   *
   * @param sortedSampledKeys A sorted list of sampled keys.
   * @param numberOfWorkers   The number of workers/partitions.
   * @return                  List of pivot keys.
   */
  def findPivotKeys(sortedSampledKeys: List[Key], numberOfWorkers: Int): List[Key] = {
    // Ensure there are enough keys for the specified number of workers
    assert(sortedSampledKeys.size >= numberOfWorkers, "Not enough keys for the specified number of workers")

    // Calculate the pivot index coefficient
    val pivotIndexCoefficient = sortedSampledKeys.size / numberOfWorkers

    // Find the value of each pivot
    (1 until numberOfWorkers).map(i => sortedSampledKeys(i * pivotIndexCoefficient)).toList
  }

  /**
   * Generates an interleaved list of pivot keys along with min and max keys.
   *
   * @param pivots List of pivot keys.
   * @return       Interleaved list of keys.
   */
  def generateInterleavedPivotList(pivots: List[Key]): List[Key] = {
    val minKey = Key(Array.fill(10)(0.toByte))
    val maxKey = Key(Array.fill(10)(255.toByte))

    // Interleave minKey, pivot, and pivot + 1, and append maxKey
    val keyList: List[Key] = List(minKey) ++ pivots.flatMap(pivot => List(pivot, pivot.incrementByOne)) ++ List(maxKey)
    keyList
  }

  /**
   * Creates a list of KeyRange objects by aggregating keys.
   *
   * @param list List of keys to be aggregated.
   * @return     List of KeyRange objects.
   */
  def createKeyRangeByAggregatingKeys(list: List[Key]): List[KeyRange] = {
    // Ensure that the list has an even number of elements
    assert(list.size % 2 == 0, "List must have an even number of elements for key range creation")

    list match {
      // If there are at least two keys in the list, create a KeyRange and recurse on the rest of the list
      case a :: b :: rest => KeyRange(a, b) :: createKeyRangeByAggregatingKeys(rest)
      // If there are fewer than two keys, return an empty list
      case _ => Nil
    }
  }

  /**
   * Performs sampling to create key ranges for partitioning.
   *
   * @param sampledKeys     List containing a sample of keys.
   * @param numberOfWorkers The number of workers/partitions.
   * @return                List of KeyRange objects representing the partitioning.
   */
  def createKeyRangeFromSampledKeys(sampledKeys: List[Key], numberOfWorkers: Int): List[KeyRange] = {
    val sortedKeys = sampledKeys.sorted
    val pivots = findPivotKeys(sortedKeys, numberOfWorkers)
    val samplingData = generateInterleavedPivotList(pivots)

    // Ensure that the sampling data is not empty
    assert(samplingData.nonEmpty, "Sampling data is empty")

    createKeyRangeByAggregatingKeys(samplingData)
  }

  /**
   * Computes the partition plan based on sampled keys and the number of workers.
   *
   * @param sampledKeys Map containing sample keys for each worker.
   * @param numWorkers  The number of workers/partitions.
   * @return            PartitionPlan object representing the partitioning plan.
   */
  def computePartitionPlan(sampledKeys: Map[String, List[Key]], numWorkers: Int): PartitionPlan = {
    // Initialize an empty array to store partitions
    var partitions: Array[(String, KeyRange)] = Array.empty
    val keyRanges = createKeyRangeFromSampledKeys(sampledKeys.values.flatten.toList, numWorkers)

    // Associate the key(i) of map sampledKeys(i) with the keyRanges(i) and add this to partitions
    // Iterate over the keys in sampledKeys and associate them with corresponding keyRanges
    sampledKeys.keys.zip(keyRanges).foreach {
      case (key, keyRange) =>
        partitions = partitions :+ (key, keyRange)
    }

    // Return the computed PartitionPlan
    PartitionPlan(partitions)
  }
}


