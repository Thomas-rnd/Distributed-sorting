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
  private var input_data_type: String = null
  private var outputFolder: String = null

  private var threadListen: Thread = null 

  def main(args: Array[String]): Unit = {
    if (args.length < 4) {
      logger.error("Usage : worker <masterIP:port> <output directory> <input data type> <input directory> <input directory> … <input directory>")
      return
    }

    // Assuming the arguments are provided in the correct order as per the example usage

    
    masterIP = args(0).split(":")(0)
    masterPort = args(0).split(":")(1).toInt
    
    // Parse input directories and output directory
    outputFolder = args(1)

    input_data_type = args(2)
    inputFolders = args.slice(3, args.length).toList


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
              if (!registerReply.success) {
                logger.info(s"Registration failed!")
                sys.exit(0)
              }
              logger.info(s"Registration done!")
              logger.info("Preparing for shuffling phase")
              threadListen = new Thread(new Runnable {
                def run(): Unit = {
                  WorkerServices.handleSaveBlockRequest(numWorkers, input_data_type)
                }
              })
              threadListen.start()
              logger.info("Listen Thread Start")
              logger.info("Preparation Done")

            case sampleKeyRequest: SampleKeyRequest =>
              logger.info("SampleKeyRequest received!")
              val out2: ObjectOutputStream = new ObjectOutputStream(socket.getOutputStream)
              var myKeys: List[Key] = List.empty[Key]
              var success = false 
              
              try {
                myKeys = inputFolders.foldLeft(List[Key]()) { (accumulatedKeys, folderPath) =>
                  accumulatedKeys ++ WorkerServices.sendSamples(folderPath, input_data_type)
                }
                success = true
              } catch {
                case e: Throwable =>
                  logger.error(s"Worker failed to compute sampleKeys : ${e.getMessage}")
              }
              
              out2.writeObject(new SampleKeyReply(success, myKeys.toArray))
              logger.info("SampleKeyReply send!")

            case savePartitionPlanRequest: SavePartitionPlanRequest =>
              logger.info("SavePartitionPlanRequest received!")
              val out2: ObjectOutputStream = new ObjectOutputStream(socket.getOutputStream)
              var success = false
              
              try {
                partitionPlan = savePartitionPlanRequest.partitionPlan
                logger.info("Partition Plan saved")
                logger.debug(partitionPlan)

                numWorkers = partitionPlan.partitions.length
                logger.debug(s"NumWorker = $numWorkers")
                success = true
              } catch {
                case e: Throwable =>
                  logger.error(s"Worker failed to save partitionPlan : ${e.getMessage}")
              }

              out2.writeObject(new SavePartitionPlanReply(success))
              logger.info("SavePartitionPlanReply send!")

              

            case sortRequest: SortRequest =>
              logger.info("SortRequest received!")
              logger.info("Sorting...")
              val out2: ObjectOutputStream = new ObjectOutputStream(socket.getOutputStream)
              var success = false

              try {
                partitionsList = inputFolders.flatMap(folderPath => WorkerServices.sortFiles(folderPath, partitionPlan, input_data_type))
                success = true
              } catch {
                case e: Throwable =>
                  logger.error(s"Worker failed to sort : ${e.getMessage}")
              }

              out2.writeObject(new SortReply(success))
              logger.info("SortReply send!")

            case shuffleRequest: ShuffleRequest =>
              logger.info("ShuffleRequest received!")
              logger.info("Shuffling...")
              val out2: ObjectOutputStream = new ObjectOutputStream(socket.getOutputStream)
              var success = false
              
              try {
                val threadSend = new Thread(new Runnable {
                  def run(): Unit = {
                    WorkerServices.sendSaveBlockRequest(partitionPlan, partitionsList, input_data_type) //partitions should be a List[Partition]
                  }
                })
                threadSend.start()
                logger.info("Sending Thread Start")

                threadListen.join()
                threadSend.join()
                success = true
              } catch {
                case e: Throwable =>
                  logger.error(s"Worker failed to shuffle : ${e.getMessage}")
              }

              
              out2.writeObject(new ShuffleReply(success))
              logger.info("ShuffleReply send!")

            case mergeRequest: MergeRequest =>
              logger.info("MergeRequest received!")
              logger.info("Merging...")
              val out2: ObjectOutputStream = new ObjectOutputStream(socket.getOutputStream)
              var success = false

              try {
                WorkerServices.mergeFiles("/tmp/sortnet_TMP/data/tmp",outputFolder,input_data_type) // TODO Dynalic tmp folder
                success = true
              } catch {
                case e: Throwable =>
                  logger.error(s"Worker failed to shuffle : ${e.getMessage}")
              }
               
              out2.writeObject(new MergeReply(success))
              logger.info("MergeReply send!")

            case terminateRequest: TerminateRequest =>
              val successMessage = if (terminateRequest.success) "Sorting is done" else s"Sorting failed ${terminateRequest.reason}"
              logger.info(s"TerminateRequest : $successMessage")
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
