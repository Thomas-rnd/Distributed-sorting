package com.cs434.sortnet.master

import java.io._
import java.net.{ServerSocket, Socket}
import java.util.concurrent.Executors
import scala.collection.mutable.{Map, HashMap, ListBuffer}

import com.cs434.sortnet.network._
import com.cs434.sortnet.core._

object Master {
  private val port: Int = 9999
  private var maxWorkers: Int = 0

  private val workerInfoMap: Map[String, WorkerInfo] = HashMap[String, WorkerInfo]()
  val sampleKeys: Map[String, List[Key]] = HashMap[String, List[Key]]()
  private var connectedWorkers: Int = 0

  def main(args: Array[String]): Unit = {
    if (args.length != 1) {
      println("Utilisation : java Master <nombre maximal de workers>")
      return
    }

    maxWorkers = args(0).toInt

    try {
      val serverSocket = new ServerSocket(port)
      println(s"Le maître écoute sur le port $port")
      val threadPool = Executors.newCachedThreadPool()

      while (connectedWorkers < maxWorkers) {
        val clientSocket = serverSocket.accept()
        println("Nouvelle connexion entrante")
        val in = new ObjectInputStream(clientSocket.getInputStream)
        val receivedObject = in.readObject
        if (receivedObject.isInstanceOf[RegisterRequest]) {
          val registerRequest = receivedObject.asInstanceOf[RegisterRequest]
          handleRegisterRequest(clientSocket, registerRequest, threadPool)
        } else {
          clientSocket.close()
        }
      }
      serverSocket.close()

      // send samplekeyrequest
      sendSampleKeyRequests()

    } catch {
      case e @ (_: IOException | _: ClassNotFoundException) =>
        e.printStackTrace()
    }
  }

  private def handleRegisterRequest(clientSocket: Socket, registerRequest: RegisterRequest, threadPool: java.util.concurrent.ExecutorService): Unit = {
    if (connectedWorkers < maxWorkers) {
      val clientIP = clientSocket.getInetAddress.getHostAddress
      println(s"IP enregistrée : $clientIP")

      // Crée un WorkerInfo avec le clientIP et la socket associée
      val workerInfo = new WorkerInfo(clientIP, clientSocket)
      workerInfoMap.put(clientIP, workerInfo)

      connectedWorkers += 1
      val reply = new RegisterReply(true)

      // Utilise la socket du client pour envoyer la RegisterReply
      sendObject(clientSocket, reply)
    }
  }

  private def sendSampleKeyRequests(): Unit = {
    val threads = ListBuffer[Thread]()

    for (workerInfo <- workerInfoMap.values) {
      val thread = new Thread(new Runnable {
        def run(): Unit = {
          val workerThreadInfo = workerInfo
          val workerSampleKeys = sampleKeys
          new SampleKeyRequestThread(workerThreadInfo, workerSampleKeys).run()
        }
      })
      threads += thread
      thread.start()
    }

    println(s"Started ${threads.size} threads for SampleKeyRequests.")

    // Attend que tous les threads aient terminé
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

    println("All samples received:")
    for ((workerIP, keys) <- sampleKeys) {
      println(s"Worker IP: $workerIP")
      println(s"Sample Keys: $keys")
    }
  }

  private def sendObject(socket: Socket, obj: Serializable): Unit = {
    try {
      val out = new ObjectOutputStream(socket.getOutputStream)
      out.writeObject(obj)
    } catch {
      case e: IOException =>
        e.printStackTrace()
    }
  }
}

class SampleKeyRequestThread(private val workerInfo: WorkerInfo, private val sampleKeys: Map[String, List[Key]]) extends Runnable {
  override def run(): Unit = {
    try {
      val socket = workerInfo.getSocket
      val out = new ObjectOutputStream(socket.getOutputStream)

      // Envoyer la requête SampleKeyRequest
      val request = new SampleKeyRequest
      out.writeObject(request)

      // Attendre la réponse SampleKeyReply
      val in = new ObjectInputStream(socket.getInputStream)
      val receivedObject = in.readObject
      if (receivedObject.isInstanceOf[SampleKeyReply]) {
        val reply = receivedObject.asInstanceOf[SampleKeyReply]
        handleSampleKeyReply(reply)
      }
    } catch {
      case e @ (_: IOException | _: ClassNotFoundException) =>
        e.printStackTrace()
    }
  }

  private def handleSampleKeyReply(sampleKeyReply: SampleKeyReply): Unit = {
    val workerIP = workerInfo.getWorkerIP
    val keys = sampleKeyReply.sampledKeys
    println(s"Keys from worker $workerIP: $keys")
    sampleKeys.put(workerIP, keys)
  }
}

class WorkerInfo(private val workerIP: String, private val socket: Socket) {
  def getWorkerIP: String = workerIP

  def getSocket: Socket = socket
}
