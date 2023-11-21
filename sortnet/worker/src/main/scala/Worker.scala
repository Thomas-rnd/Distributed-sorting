package com.cs434.sortnet.worker

import java.io._
import java.net._
import java.util.{ ArrayList }
import scala.jdk.CollectionConverters

import com.cs434.sortnet.network._
import com.cs434.sortnet.core._

import org.apache.logging.log4j.scala.Logging

object Worker extends Logging{

  private var masterIP: String = null
  private var masterPort: Int = 0

  private var numWorkers: Int = 0
  private var partitionsList: List[Partition] = null
  private var partitionPlan: PartitionPlan = null
  private var inputFolders: List[String] = List()
  private var outputFolder: String = null

  private var threadListen: Thread = null 

  def main(args: Array[String]): Unit = {
    if (args.length < 3) {
      logger.error("Usage : worker <masterIP:port> <output directory> <input directory> <input directory> … <input directory>")
      return
    }

    // Assuming the arguments are provided in the correct order as per the example usage

    
    masterIP = args(0).split(":")(0)
    masterPort = args(0).split(":")(1).toInt
    
    // Parse input directories and output directory
    outputFolder = args(1)
    inputFolders = args.slice(2, args.length).toList


    // Printing parsed values for demonstration
    logger.info(s"Master IP: $masterIP")
    logger.info(s"Master Port: $masterPort")
    logger.info(s"Input Folders: $inputFolders")
    logger.info(s"Output Folder: $outputFolder")

    threadListen = new Thread()

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
              logger.info(s"Registration done!")

            case sampleKeyRequest: SampleKeyRequest =>
              logger.info("SampleKeyRequest received!")

              var myKeys: List[Key] = inputFolders.foldLeft(List[Key]()) { (accumulatedKeys, folderPath) =>
                accumulatedKeys ++ WorkerServices.sendSamples(folderPath)
              }

              val reply: SampleKeyReply = new SampleKeyReply(true, myKeys.toArray)
              
              
              val out2: ObjectOutputStream = new ObjectOutputStream(socket.getOutputStream)
              out2.writeObject(reply)
              logger.info("SampleKeyReply send!")

            case savePartitionPlanRequest: SavePartitionPlanRequest =>
              logger.info("SavePartitionPlanRequest received!")

              partitionPlan = savePartitionPlanRequest.partitionPlan
              logger.info("Partition Plan saved :")
              logger.info(partitionPlan)

              numWorkers = partitionPlan.partitions.length
              logger.info(s"NumWorker = $numWorkers")

              logger.info("Preparing for shuffling phase")
              threadListen = new Thread(new Runnable {
                def run(): Unit = {
                  WorkerServices.handleSaveBlockRequest(numWorkers)
                }
              })
              threadListen.start()
              logger.info("Listen Thread Start")
              logger.info("Preparation Done")

              val reply: SavePartitionPlanReply = new SavePartitionPlanReply(true)
              val out2: ObjectOutputStream = new ObjectOutputStream(socket.getOutputStream)
              out2.writeObject(reply)
              logger.info("SavePartitionPlanReply send!")

            case sortRequest: SortRequest =>
              logger.info("SortRequest received!")
              logger.info("Sorting...")

              partitionsList = inputFolders.flatMap(folderPath => WorkerServices.sortFiles(folderPath, partitionPlan))

              val reply: SortReply = new SortReply(true)
              val out2: ObjectOutputStream = new ObjectOutputStream(socket.getOutputStream)
              out2.writeObject(reply)
              logger.info("SortReply send!")

            case shuffleRequest: ShuffleRequest =>
              logger.info("ShuffleRequest received!")
              logger.info("Shuffling...")

              val threadSend = new Thread(new Runnable {
                def run(): Unit = {
                  WorkerServices.sendSaveBlockRequest(partitionPlan, partitionsList) //partitions should be a List[Partition]
                }
              })
              threadSend.start()
              logger.info("Sending Thread Start")

              threadListen.join()
              threadSend.join()

              val reply: ShuffleReply = new ShuffleReply(true)
              val out2: ObjectOutputStream = new ObjectOutputStream(socket.getOutputStream)
              out2.writeObject(reply)
              logger.info("ShuffleReply send!")

            case mergeRequest: MergeRequest =>
              logger.info("MergeRequest received!")
              logger.info("Merging...")
              WorkerServices.mergeFiles("/home/red/data/tmp")
              val reply: MergeReply = new MergeReply(true)
              val out2: ObjectOutputStream = new ObjectOutputStream(socket.getOutputStream)
              out2.writeObject(reply)
              logger.info("MergeReply send!")

            case terminateRequest: TerminateRequest =>
              logger.info("TerminateRequest received!")
              logger.info("Terminating...")
              val reply: TerminateReply = new TerminateReply(true)
              val out2: ObjectOutputStream = new ObjectOutputStream(socket.getOutputStream)
              out2.writeObject(reply)
              logger.info("TerminateReply send!")
              isDone = true
          }
        } catch {
          case e: ClassNotFoundException =>
            logger.error(s"${e.getMessage}", e)
        }
      }

      socket.close()
    } catch {
      case e: IOException =>
        logger.error(s"${e.getMessage}", e)
    }
  }
}
