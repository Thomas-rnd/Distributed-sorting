package com.cs434.sortnet.master

import java.io._
import java.net.{ServerSocket, Socket}
import java.util.concurrent.Executors
import scala.collection.mutable.{Map, HashMap, ListBuffer}

import com.cs434.sortnet.network._
import com.cs434.sortnet.core._

object Master {
  private val port: Int = 9999
  private var numWorkers: Int = 0

  private val workerMetadataMap: Map[String, WorkerMetadata] = HashMap[String, WorkerMetadata]()
  val sampleKeys: Map[String, List[Key]] = HashMap[String, List[Key]]()
  private var connectedWorkers: Int = 0

  def main(args: Array[String]): Unit = {
    if (args.length != 1) {
      println("Utilisation : java Master <nombre maximal de workers>")
      return
    }

    numWorkers = args(0).toInt

    try {
//======================================================      
      println("Stage start : Register")
      val serverSocket = new ServerSocket(port)
      println(s"Le maître écoute sur le port $port")
      val threadPool = Executors.newCachedThreadPool()

      while (workerMetadataMap.size < numWorkers) {
        val clientSocket = serverSocket.accept()
        println("Nouvelle connexion entrante")
        val in = new ObjectInputStream(clientSocket.getInputStream)
        val receivedObject = in.readObject
        if (receivedObject.isInstanceOf[RegisterRequest]) {
          val registerRequest = receivedObject.asInstanceOf[RegisterRequest]
          
          MasterServices.handleRegisterRequest(clientSocket, registerRequest, threadPool, workerMetadataMap,numWorkers)
          
          
        } else {
          clientSocket.close()
        }
      }
      serverSocket.close()

      println(s"All $numWorkers workers register")
      println("Server socket close")
      println("Stage end : Register")

//======================================================
      println("Stage start : Sampling")
      // broadcast samplekeyrequest
      MasterServices.sendRequests(workerMetadataMap, MessageType.SampleKey, sampleKeys = Some(sampleKeys))
      println("Stage end : Sampling")

//======================================================
      println("Stage start : Partitioning")
      // compute partitionPlan
      val partitionPlan = MasterServices.computePartitionPlan(sampleKeys, numWorkers)
      // broadcast partitionPlan
      MasterServices.sendRequests(workerMetadataMap, MessageType.SavePartitionPlan, partitionPlan = Some(partitionPlan))
      println("Stage end : Partitioning")

//======================================================
      println("Stage start : Sorting")
      // broadcast sortRequest
      MasterServices.sendRequests(workerMetadataMap, MessageType.Sort)
      println("Stage end : Sorting")

//======================================================
      println("Stage start : Shuffling")
      // broadcast shuffleRequest
      MasterServices.sendRequests(workerMetadataMap, MessageType.Shuffle)
      println("Stage end : Shuffling")

//======================================================
      println("Stage start : Merge")
      // broadcast mergeRequest
      MasterServices.sendRequests(workerMetadataMap, MessageType.Merge)
      println("Stage end : Merge")

//======================================================
      println("Stage start : Terminate")
      // broadcast terminateRequest
      MasterServices.sendRequests(workerMetadataMap, MessageType.Terminate)
      println("Stage end : Terminate")

    } catch {
      case e @ (_: IOException | _: ClassNotFoundException) =>
        e.printStackTrace()
    }
  }
}
