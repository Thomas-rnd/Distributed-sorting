package com.cs434.sortnet.worker

import java.io._
import java.net._
import java.util.{ArrayList}
import scala.collection.JavaConverters._

import com.cs434.sortnet.network._
import com.cs434.sortnet.core._

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
              println(s"Registration done!")

            case sampleKeyRequest: SampleKeyRequest =>
              println("SampleKeyRequest received!")
              val keyBytes: Array[Byte] = Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) // Example byte array
              val a: Key = Key(keyBytes)
              val mykeys: List[Key] = List(a)
              val reply: SampleKeyReply = new SampleKeyReply(true,mykeys)
              val out2: ObjectOutputStream = new ObjectOutputStream(socket.getOutputStream)
              out2.writeObject(reply)
              println("SampleKeyReply send!")

            case savePartitionPlanRequest: SavePartitionPlanRequest =>
              println("SavePartitionPlanRequest received!")
              println(savePartitionPlanRequest.partitionPlan)
              val reply: SavePartitionPlanReply = new SavePartitionPlanReply(true)
              val out2: ObjectOutputStream = new ObjectOutputStream(socket.getOutputStream)
              out2.writeObject(reply)
              println("SavePartitionPlanReply send!")
            
            case sortRequest: SortRequest =>
              println("SortRequest received!")
              println("Sorting...")
              val reply: SortReply = new SortReply(true)
              val out2: ObjectOutputStream = new ObjectOutputStream(socket.getOutputStream)
              out2.writeObject(reply)
              println("SortReply send!")

            case shuffleRequest: ShuffleRequest =>
              println("ShuffleRequest received!")
              println("Shuffling...")
              val reply: ShuffleReply = new ShuffleReply(true)
              val out2: ObjectOutputStream = new ObjectOutputStream(socket.getOutputStream)
              out2.writeObject(reply)
              println("ShuffleReply send!")

            case mergeRequest: MergeRequest =>
              println("MergeRequest received!")
              println("Merging...")
              val reply: MergeReply = new MergeReply(true)
              val out2: ObjectOutputStream = new ObjectOutputStream(socket.getOutputStream)
              out2.writeObject(reply)
              println("MergeReply send!")

            case terminateRequest: TerminateRequest =>
              println("TerminateRequest received!")
              println("Terminating...")
              val reply: TerminateReply = new TerminateReply(true)
              val out2: ObjectOutputStream = new ObjectOutputStream(socket.getOutputStream)
              out2.writeObject(reply)
              println("TerminateReply send!")
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
