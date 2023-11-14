package com.cs434.sortnet.master

import java.io._
import java.util.concurrent.{ExecutorService, Executors}
import java.net.{ServerSocket, Socket}
import scala.collection.mutable.{Map, HashMap, ListBuffer}

import com.cs434.sortnet.network._
import com.cs434.sortnet.core._

object  MasterServices {

  def handleRegisterRequest(clientSocket: Socket, registerRequest: RegisterRequest, threadPool: ExecutorService, workerMetadataMap: Map[String, WorkerMetadata], numWorkers: Int): Unit = {
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
                            sendRequestThread(workerThreadMetadata, MessageType.SampleKey, sampleKeys=sampleKeys)
                            /*sampleKeys match {
                                case Some(sk) => 
                                    sendRequestThread(workerMetadataMap, MessageType.SampleKey, sampleKeys = Some(sk))
                                    sendSampleKeyRequestThread(workerThreadMetadata, sampleKeys)
                                case None =>
                                    println("sampleKeys not provided for SampleKey")
                            }*/
                            
                        case MessageType.SavePartitionPlan => 
                            sendRequestThread(workerThreadMetadata, MessageType.SavePartitionPlan, partitionPlan=partitionPlan)
                            /*partitionPlan match {
                                case Some(plan) =>
                                    sendSavePartitionPlanRequestThread(workerThreadMetadata, plan)
                                case None =>
                                    println("partitionPlan not provided for SavePartitionPlan")
                            }*/
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

        println(s"Started ${threads.size} threads for ${messageType.toString} Requests.")

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
                // Send the SampleKeyRequest
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
                // Send the SavePartitionPlanRequest
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

  def findPivot(sortedSampledKeys: List[Key], numberOfWorkers: Int): List[Key] = {
    // Pivot index coefficient
    assert(sortedSampledKeys.size >= numberOfWorkers, "Not enough keys for the specified number of workers")
    val pivot = sortedSampledKeys.size / numberOfWorkers
    // Find value of each pivot
    (1 until numberOfWorkers).map(i => sortedSampledKeys(i * pivot)).toList
  }

  def generateInterleavedPivotList(pivots: List[Key]): List[Key] = {
    val minKey = Key(Array.fill(10)(0.toByte))
    val maxKey = Key(Array.fill(10)(127.toByte))
    // Interleave minKey, pivot, and pivot + 1, and append maxKey
    val keyList : List[Key]=List(minKey) ++ pivots.flatMap(pivot => List(pivot, pivot.incrementByOne)) ++ List(maxKey)
    keyList
  }

  def createKeyRangeByAggregatingKeys(list: List[Key]): List[KeyRange] = list match {
    case a :: b :: rest => KeyRange(a, b) :: createKeyRangeByAggregatingKeys(rest)
    case _ => Nil
  }

  def sampling(sampledKeys: List[Key], numberOfWorkers: Int): List[KeyRange] = {
    val sortedKeys = sampledKeys.sorted
    val pivots = findPivot(sortedKeys,numberOfWorkers)
    val samplingData = generateInterleavedPivotList(pivots)
    val keyRanges = createKeyRangeByAggregatingKeys(samplingData)
    keyRanges
  }

  def computePartitionPlan(sampleKeys: Map[String, List[Key]], numWorkers: Int): PartitionPlan = {
    
    var partitions: List[(String, KeyRange)] = List.empty
    val keyRanges = sampling(sampleKeys.values.flatten.toList, numWorkers)

    // assiociate the key(i) of map sampleKeys(i) with the keyRangs(i)
    // and add this in partitions
    // Iterate over the keys in sampleKeys and associate them with corresponding keyRanges
    sampleKeys.keys.zip(keyRanges).foreach {
        case (key, keyRange) =>
        partitions = partitions :+ (key, keyRange)
    }

    PartitionPlan(partitions)
  }
}
