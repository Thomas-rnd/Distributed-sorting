package com.cs434.sortnet.worker

import java.io._
import java.net._
import java.util.{ArrayList}
import scala.jdk.CollectionConverters

import com.cs434.sortnet.network._
import com.cs434.sortnet.core._

object Worker {

  private var masterIP: String = null
  private val masterPort: Int = 9999

  private var numWorkers: Int = 0
  private var partitionsList: List[Partition] = null
  private var partitionPlan: PartitionPlan = null
  private var inputFolderPath: List[String] = List()

  private var threadListen: Thread = null

  def main(args: Array[String]): Unit = {
    if (args.length < 2) {
      System.out.println(
        "Utilisation : java Worker <adresse IP du maître> <input folder 1> [<input folder ...>]"
      )
      return
    }

    masterIP = args(0)
    // Concatenate input folders to the list starting from index 1
    inputFolderPath = inputFolderPath ++ args.slice(1, args.length).toList

    // Print the values in the inputFolderPath list
    println("Input Folders: " + inputFolderPath.mkString(", "))

    val testImputfolder = "/home/red/data/input"

    threadListen = new Thread()

    try {
      val socket: Socket = new Socket(masterIP, masterPort)
      val out: ObjectOutputStream = new ObjectOutputStream(
        socket.getOutputStream
      )
      val request: RegisterRequest = new RegisterRequest
      out.writeObject(request) // Envoie un message d'enregistrement
      var isDone: Boolean = false

      while (!isDone) {
        try {
          val in: ObjectInputStream = new ObjectInputStream(
            socket.getInputStream
          )
          val receivedObject: AnyRef = in.readObject
          receivedObject match {

            case registerReply: RegisterReply =>
              println(s"Registration done!")

            case sampleKeyRequest: SampleKeyRequest =>
              println("SampleKeyRequest received!")

              val mykeys = WorkerServices.sendSamples(testImputfolder)
              val reply: SampleKeyReply = new SampleKeyReply(true, mykeys)
              val out2: ObjectOutputStream = new ObjectOutputStream(
                socket.getOutputStream
              )
              out2.writeObject(reply)
              println("SampleKeyReply send!")

            case savePartitionPlanRequest: SavePartitionPlanRequest =>
              println("SavePartitionPlanRequest received!")

              partitionPlan = savePartitionPlanRequest.partitionPlan
              println("Partition Plan saved :")
              println(partitionPlan)

              numWorkers = partitionPlan.partitions.length
              println(s"NumWorker = $numWorkers")

              println("Preparing for shuffling phase")
              threadListen = new Thread(new Runnable {
                def run(): Unit = {
                  WorkerServices.handleSaveBlockRequest(numWorkers)
                }
              })
              threadListen.start()
              println("Listen Thread Start")
              println("Preparation Done")

              val reply: SavePartitionPlanReply = new SavePartitionPlanReply(
                true
              )
              val out2: ObjectOutputStream = new ObjectOutputStream(
                socket.getOutputStream
              )
              out2.writeObject(reply)
              println("SavePartitionPlanReply send!")

            case sortRequest: SortRequest =>
              println("SortRequest received!")
              println("Sorting...")
              partitionsList =
                WorkerServices.sortFiles(testImputfolder, partitionPlan)

              val reply: SortReply = new SortReply(true)
              val out2: ObjectOutputStream = new ObjectOutputStream(
                socket.getOutputStream
              )
              out2.writeObject(reply)
              println("SortReply send!")

            case shuffleRequest: ShuffleRequest =>
              println("ShuffleRequest received!")
              println("Shuffling...")

              val threadSend = new Thread(new Runnable {
                def run(): Unit = {
                  WorkerServices.sendSaveBlockRequest(
                    partitionPlan,
                    partitionsList
                  ) // partitions should be a List[Partition]
                }
              })
              threadSend.start()
              println("Sending Thread Start")

              threadListen.join()
              threadSend.join()

              val reply: ShuffleReply = new ShuffleReply(true)
              val out2: ObjectOutputStream = new ObjectOutputStream(
                socket.getOutputStream
              )
              out2.writeObject(reply)
              println("ShuffleReply send!")

            case mergeRequest: MergeRequest =>
              println("MergeRequest received!")
              println("Merging...")
              WorkerServices.mergeFiles("/home/red/data/tmp")
              val reply: MergeReply = new MergeReply(true)
              val out2: ObjectOutputStream = new ObjectOutputStream(
                socket.getOutputStream
              )
              out2.writeObject(reply)
              println("MergeReply send!")

            case terminateRequest: TerminateRequest =>
              println("TerminateRequest received!")
              println("Terminating...")
              val reply: TerminateReply = new TerminateReply(true)
              val out2: ObjectOutputStream = new ObjectOutputStream(
                socket.getOutputStream
              )
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
