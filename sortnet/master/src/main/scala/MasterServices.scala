package com.cs434.sortnet.master

import java.io._
import java.util.concurrent.{ExecutorService, Executors}
import java.net.{ServerSocket, Socket}
import scala.collection.mutable.{Map, HashMap, ListBuffer}

import com.cs434.sortnet.network._
import com.cs434.sortnet.core._

object MasterServices {

  def handleRegisterRequest(
      clientSocket: Socket,
      registerRequest: RegisterRequest,
      threadPool: ExecutorService,
      workerMetadataMap: Map[String, WorkerMetadata],
      numWorkers: Int
  ): Unit = {
    if (workerMetadataMap.size < numWorkers) {
      val clientIP = clientSocket.getInetAddress.getHostAddress
      println(s"IP enregistrée : $clientIP")

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
          e.printStackTrace()
      }
    }
  }

  def sendRequests(
      workerMetadataMap: Map[String, WorkerMetadata],
      messageType: MessageType.Value,
      partitionPlan: Option[PartitionPlan] = None,
      sampleKeys: Option[Map[String, List[Key]]] = None
  ): Unit = {
    val threads = ListBuffer[Thread]()

    for (workerMetadata <- workerMetadataMap.values) {
      val thread = new Thread(new Runnable {
        def run(): Unit = {
          val workerThreadMetadata = workerMetadata
          messageType match {
            case MessageType.SampleKey =>
              sendRequestThread(
                workerThreadMetadata,
                MessageType.SampleKey,
                sampleKeys = sampleKeys
              )

            case MessageType.SavePartitionPlan =>
              sendRequestThread(
                workerThreadMetadata,
                MessageType.SavePartitionPlan,
                partitionPlan = partitionPlan
              )

            case MessageType.Sort =>
              sendRequestThread(workerThreadMetadata, MessageType.Sort)

            case MessageType.Shuffle =>
              sendRequestThread(workerThreadMetadata, MessageType.Shuffle)

            case MessageType.Merge =>
              sendRequestThread(workerThreadMetadata, MessageType.Merge)

            case MessageType.Terminate =>
              sendRequestThread(workerThreadMetadata, MessageType.Terminate)

            case _ =>
              println(s"Unsupported message type: $messageType")
          }
        }
      })
      threads += thread
      thread.start()
    }

    println(
      s"Started ${threads.size} threads for ${messageType.toString} Requests."
    )

    // Wait for all the threads to finish
    for (thread <- threads) {
      try {
        thread.join()
        println(s"Thread joined: ${thread.getId}")
      } catch {
        case e: InterruptedException =>
          e.printStackTrace()
          System.err.println(s"Thread join interrupted: ${e.getMessage}")
      }
    }

    // Log relevant information based on the message type
    messageType match {
      case MessageType.SampleKey =>
        sampleKeys match {
          case Some(sampleKeysFound) =>
            println("All samples received:")
            for ((workerIP, keys) <- sampleKeysFound) {
              println(s"Worker IP: $workerIP")
              println(s"Sample Keys: $keys")
            }
          case None =>
            println("sampleKeys not provided for SampleKey")
        }

      case _ => // Handle other message types
    }
  }

  def sendRequestThread(
      workerMetadata: WorkerMetadata,
      messageType: MessageType.Value,
      partitionPlan: Option[PartitionPlan] = None,
      sampleKeys: Option[Map[String, List[Key]]] = None
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
                val workerIP = workerMetadata.ip
                val keys = reply.sampledKeys
                println(s"Keys from worker $workerIP: $keys")
                sampleKeysFound.put(workerIP, keys)
              }
            case None =>
              println("sampleKeys not provided for SampleKey")
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
              }
            case None =>
              println("partitionPlan not provided for SavePartitionPlan")
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
          }

        case MessageType.Terminate =>
          // Send the TerminateRequest
          val request = new TerminateRequest
          out.writeObject(request)

          // Wait for the TerminateReply
          val in = new ObjectInputStream(socket.getInputStream)
          val receivedObject = in.readObject
          if (receivedObject.isInstanceOf[TerminateReply]) {
            val reply = receivedObject.asInstanceOf[TerminateReply]
          }

        case _ =>
          println(s"Unsupported message type: $messageType")
      }
    } catch {
      case e @ (_: IOException | _: ClassNotFoundException) =>
        e.printStackTrace()
    }
  }

  /** Finds pivot keys in a sorted list for partitioning.
    *
    * @param sortedSampledKeys
    *   A sorted list of sampled keys.
    * @param numberOfWorkers
    *   The number of workers/partitions.
    * @return
    *   List of pivot keys.
    */
  def findPivotKeys(
      sortedSampledKeys: List[Key],
      numberOfWorkers: Int
  ): List[Key] = {
    // Ensure there are enough keys for the specified number of workers
    assert(
      sortedSampledKeys.size >= numberOfWorkers,
      "Not enough keys for the specified number of workers"
    )

    // Calculate the pivot index coefficient
    val pivotIndexCoefficient = sortedSampledKeys.size / numberOfWorkers

    // Find the value of each pivot
    (1 until numberOfWorkers)
      .map(i => sortedSampledKeys(i * pivotIndexCoefficient))
      .toList
  }

  /** Generates an interleaved list of pivot keys along with min and max keys.
    *
    * @param pivots
    *   List of pivot keys.
    * @return
    *   Interleaved list of keys.
    */
  def generateInterleavedPivotList(pivots: List[Key]): List[Key] = {
    val minKey = Key(Array.fill(10)(0.toByte))
    val maxKey = Key(Array.fill(10)(127.toByte))

    // Interleave minKey, pivot, and pivot + 1, and append maxKey
    val keyList: List[Key] = List(minKey) ++ pivots.flatMap(pivot =>
      List(pivot, pivot.incrementByOne)
    ) ++ List(maxKey)
    keyList
  }

  /** Creates a list of KeyRange objects by aggregating keys.
    *
    * @param list
    *   List of keys to be aggregated.
    * @return
    *   List of KeyRange objects.
    */
  def createKeyRangeByAggregatingKeys(list: List[Key]): List[KeyRange] = {
    // Ensure that the list has an even number of elements
    assert(
      list.size % 2 == 0,
      "List must have an even number of elements for key range creation"
    )

    list match {
      // If there are at least two keys in the list, create a KeyRange and recurse on the rest of the list
      case a :: b :: rest =>
        KeyRange(a, b) :: createKeyRangeByAggregatingKeys(rest)
      // If there are fewer than two keys, return an empty list
      case _ => Nil
    }
  }

  /** Performs sampling to create key ranges for partitioning.
    *
    * @param sampledKeys
    *   List containing a sample of keys.
    * @param numberOfWorkers
    *   The number of workers/partitions.
    * @return
    *   List of KeyRange objects representing the partitioning.
    */
  def createKeyRangeFromSampledKeys(
      sampledKeys: List[Key],
      numberOfWorkers: Int
  ): List[KeyRange] = {
    val sortedKeys = sampledKeys.sorted
    val pivots = findPivotKeys(sortedKeys, numberOfWorkers)
    val samplingData = generateInterleavedPivotList(pivots)

    // Ensure that the sampling data is not empty
    assert(samplingData.nonEmpty, "Sampling data is empty")

    createKeyRangeByAggregatingKeys(samplingData)
  }

  /** Computes the partition plan based on sampled keys and the number of
    * workers.
    *
    * @param sampledKeys
    *   Map containing sample keys for each worker.
    * @param numWorkers
    *   The number of workers/partitions.
    * @return
    *   PartitionPlan object representing the partitioning plan.
    */
  def computePartitionPlan(
      sampledKeys: Map[String, List[Key]],
      numWorkers: Int
  ): PartitionPlan = {
    // Initialize an empty list to store partitions
    var partitions: List[(String, KeyRange)] = List.empty
    val keyRanges = createKeyRangeFromSampledKeys(
      sampledKeys.values.flatten.toList,
      numWorkers
    )

    // Associate the key(i) of map sampledKeys(i) with the keyRanges(i) and add this to partitions
    // Iterate over the keys in sampledKeys and associate them with corresponding keyRanges
    sampledKeys.keys.zip(keyRanges).foreach { case (key, keyRange) =>
      partitions = partitions :+ (key, keyRange)
    }

    // Return the computed PartitionPlan
    PartitionPlan(partitions)
  }
}
