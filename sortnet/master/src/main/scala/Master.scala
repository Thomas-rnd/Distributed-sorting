package com.cs434.sortnet.master

import java.io._
import java.net.{ServerSocket, Socket, InetAddress}
import java.util.concurrent.Executors
import scala.collection.mutable.{Map, HashMap, ListBuffer}

import com.cs434.sortnet.network._
import com.cs434.sortnet.core._

import org.apache.logging.log4j.scala.Logging
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager

object Master extends Logging{
  private val port: Int = 9999
  private var numWorkers: Int = 0
  private var connectedWorkers: Int = 0
  
  private val workerMetadataMap: Map[String, WorkerMetadata] = HashMap[String, WorkerMetadata]()
  val sampleKeys: Map[String, List[Key]] = HashMap[String, List[Key]]()
  

  def main(args: Array[String]): Unit = {
    if (args.length != 1) {
      logger.error("Usage : master <# of workers>")
      return
    }

    numWorkers = args(0).toInt
    val myIP = InetAddress.getLocalHost.getHostAddress
    // Get the underlying Log4j Logger
    val log4jLogger = LogManager.getLogger(getClass.getName)

    // Use the Log4j Logger with your custom log level
    log4jLogger.log(Level.forName("SPEC", Level.INFO.intLevel), s"$myIP:$port")
  

    try {
//======================================================      
      logger.info("Stage start : Register")
      val serverSocket = new ServerSocket(port)
      logger.info(s"Master listening on $port , waiting for $numWorkers workers")
      val threadPool = Executors.newCachedThreadPool()

      while (workerMetadataMap.size < numWorkers) {
        val clientSocket = serverSocket.accept()
        logger.info("New incoming connexion")
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

      logger.info(s"All $numWorkers workers register")
      
      val orderedWorkers = workerMetadataMap.values.toList.sortBy(_.ip)

      // Update workerMetadataMap with ordered workers
      workerMetadataMap.clear()
      orderedWorkers.foreach(workerMetadata => workerMetadataMap.put(workerMetadata.ip, workerMetadata))

      log4jLogger.log(Level.forName("SPEC", Level.INFO.intLevel), "Ordered list of workers:")
      workerMetadataMap.values.foreach { workerMetadata =>
        log4jLogger.log(Level.forName("SPEC", Level.INFO.intLevel), s"Worker: IP - ${workerMetadata.ip}")
      }
      logger.info("Server socket close")
      logger.info("Stage end : Register")

//======================================================
      logger.info("Stage start : Sampling")
      // broadcast samplekeyrequest
      MasterServices.sendRequests(workerMetadataMap, MessageType.SampleKey, sampleKeys = Some(sampleKeys))
      logger.info("Stage end : Sampling")

//======================================================
      logger.info("Stage start : Partitioning")
      // compute partitionPlan
      var partitionPlan = new PartitionPlan(null)
      try {
        partitionPlan = MasterServices.computePartitionPlan(sampleKeys, numWorkers)
      } catch {
        case e: Throwable =>
          throw new MasterTaskError(s"Master failed to compute partitionPlan : ${e.getMessage}")
      }
      // broadcast partitionPlan
      MasterServices.sendRequests(workerMetadataMap, MessageType.SavePartitionPlan, partitionPlan = Some(partitionPlan))
      logger.info("Stage end : Partitioning")

//======================================================
      logger.info("Stage start : Sorting")
      // broadcast sortRequest
      MasterServices.sendRequests(workerMetadataMap, MessageType.Sort)
      logger.info("Stage end : Sorting")

//======================================================
      logger.info("Stage start : Shuffling")
      // broadcast shuffleRequest
      MasterServices.sendRequests(workerMetadataMap, MessageType.Shuffle)
      logger.info("Stage end : Shuffling")

//======================================================
      logger.info("Stage start : Merge")
      // broadcast mergeRequest
      MasterServices.sendRequests(workerMetadataMap, MessageType.Merge)
      logger.info("Stage end : Merge")

//======================================================
      logger.info("Stage start : Terminate")
      // broadcast terminateRequest
      MasterServices.sendRequests(workerMetadataMap, MessageType.Terminate, success=Some(true), reason=Some("All workers are done!"))
      logger.info("Stage end : Terminate")

    } catch {
      case e: IOException =>
        logger.error(s"${e.getMessage}", e)

      case e: WorkerFailed =>
        logger.error(s"${e.getMessage}")
        logger.info("Send Terminate request with failed status to workers")
        //workerMetadataMap.remove(e.getWorkerIP)
        MasterServices.sendRequests(workerMetadataMap, MessageType.Terminate, success=Some(false), reason=Some(s"Worker Failed:${e.getMessage}"))
      
      case e: WorkerError =>
        logger.error(s"Worker Error: ${e.getMessage}")
        logger.info(s"Remove worker ${e.getWorkerIP} from workerMetadataMap")
        workerMetadataMap.remove(e.getWorkerIP)
        logger.info("Send Terminate request with failed status to workers")
        MasterServices.sendRequests(workerMetadataMap, MessageType.Terminate, success=Some(false), reason=Some(s"Worker Error: workerIP close connection"))

      case e: MasterTaskError =>
        logger.error(s"MasterTaskError Error : ${e.getMessage}")
        MasterServices.sendRequests(workerMetadataMap, MessageType.Terminate, success=Some(false), reason=Some(s"MasterTaskError Error: ${e.getMessage}"))

      case e: ClassNotFoundException =>
        logger.error(s"ClassNotFoundException : ${e.getMessage}")
        MasterServices.sendRequests(workerMetadataMap, MessageType.Terminate, success=Some(false), reason=Some(s"Master Error: ClassNotFoundException"))

      case e: RuntimeException =>
        logger.error(s"RuntimeException Error : ${e.getMessage}")
        MasterServices.sendRequests(workerMetadataMap, MessageType.Terminate, success=Some(false), reason=Some(s"Master Error: RuntimeException"))
      
      case e: Throwable  =>
        logger.error(s"Throwable : ${e.getMessage}")
        MasterServices.sendRequests(workerMetadataMap, MessageType.Terminate, success=Some(false), reason=Some(s"Master Error: Throwable ${e.getMessage}"))

    }

    for ((workerId, workerMetadata) <- workerMetadataMap) {
      try {
        workerMetadata.socket.close()
        logger.debug(s"Closed socket for worker $workerId")
      } catch {
        case e: IOException =>
          logger.error(s"Error closing socket for worker $workerId: ${e.getMessage}")
          // Handle the exception if needed
      }
    }
  }
}
