package com.cs434.sortnet.worker

import java.io._
import java.net._
import java.util.{ArrayList}
import scala.collection.JavaConverters._

import com.cs434.sortnet.network
import com.cs434.sortnet.core

object Worker {
  def main(args: Array[String]): Unit = {
    if (args.length != 1) {
      System.out.println("Utilisation : java Worker <adresse IP du maître>")
      return
    }

    val masterIP: String = args(0)
    val masterPort: Int = 9999

    try {
      val socket: Socket = new Socket(masterIP, masterPort)
      val out: ObjectOutputStream = new ObjectOutputStream(socket.getOutputStream)
      val request: RegisterRequest = new RegisterRequest
      out.writeObject(request) // Envoie un message d'enregistrement
      var isDone: Boolean = false

      while (!isDone) {
        try {
          val in: ObjectInputStream = new ObjectInputStream(socket.getInputStream)
          val receivedObject: AnyRef = in.readObject
          receivedObject match {
            case registerReply: RegisterReply =>
              System.out.println(s"Message reçu : ${registerReply.getMessage}")

            case samplingKeyRequest: SamplingKeyRequest =>
              val mykeys: List[String] = List("hello", "world", "red")
              val reply: SamplingKeyReply = new SamplingKeyReply(mykeys)


              val out2: ObjectOutputStream = new ObjectOutputStream(socket.getOutputStream)
              out2.writeObject(reply)

              isDone = true
          }
        } catch {
          case e: ClassNotFoundException =>
            e.printStackTrace()
        }
      }

      socket.close()
    } catch {
      case e: IOException =>
        e.printStackTrace()
    }
  }
}
