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




    // Calculate key ranges from sampled keys received from workers
  def calculateKeyRanges(sampledKeys: List[Key], numWorkers: Int): List[KeyRange] = {
    val sortedKeys = sampledKeys.sorted
    val numPivots = numWorkers - 1
    // Calculating the indices of pivot points
    val pivotIndices = (1 to numPivots).map { i =>
      (i * sortedKeys.size) / numWorkers
    }
    // Creating KeyRange tuples based on pivot points
    pivotIndices.zip(pivotIndices.tail :+ sortedKeys.size).map {
      case (startIdx, endIdx) =>
        val startKey = sortedKeys(startIdx)
        val endKey = if (endIdx < sortedKeys.size) sortedKeys(endIdx - 1) else Key(Array()) // Replace with the appropriate Key
        KeyRange(startKey, Some(endKey))
    }.toList
  }

  def computePartitionPlan(sampleKeys: Map[String, List[Key]], numWorkers: Int): PartitionPlan = {
    val keyBytes1: Array[Byte] = Array(0, 0, 0, 0, 0, 0, 0, 0, 0, 0) // Example byte array
    val keyBytes2: Array[Byte] = Array(0, 0, 0, 0, 0, 0, 0, 0, 0, 1)
    val keyBytes3: Array[Byte] = Array(0, 0, 0, 0, 0, 0, 0, 0, 0, 2)
    val keyBytes4: Array[Byte] = Array(0, 0, 0, 0, 0, 0, 0, 0, 0, 3)
    val keyBytes5: Array[Byte] = Array(0, 0, 0, 0, 0, 0, 0, 0, 0, 4)
    val keyBytes6: Array[Byte] = Array(0, 0, 0, 0, 0, 0, 0, 0, 0, 5)
    val k1: Key = Key(keyBytes1)
    val k2: Key = Key(keyBytes2)
    val k3: Key = Key(keyBytes3)
    val k4: Key = Key(keyBytes4)
    val k5: Key = Key(keyBytes5)
    val k6: Key = Key(keyBytes6)
    
    
    
    val keyRange1 = KeyRange(k1, Some(k2))
    val keyRange2 = KeyRange(k3, Some(k4))
    val keyRange3 = KeyRange(k5, Some(k6))

    val partitions = List(
    ("Worker1", keyRange1),
    ("Worker2", keyRange2),
    ("Worker3", keyRange3)
    )

    PartitionPlan(partitions)
  }



 


}
