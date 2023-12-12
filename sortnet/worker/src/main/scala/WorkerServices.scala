package com.cs434.sortnet.worker

import java.io._
import java.util.concurrent.{ExecutorService, Executors}
import java.net.{ServerSocket, Socket, InetAddress}
import java.nio.file.{Files, Paths}
import scala.collection.mutable.{Map, HashMap, ListBuffer}
import java.lang.Thread.UncaughtExceptionHandler

import com.cs434.sortnet.network._
import com.cs434.sortnet.core._

import org.apache.logging.log4j.scala.Logging

object  WorkerServices extends Logging{

  /**
    * Sends sampled keys from the specified folder.
    *
    * @param folderPath     Path to the folder containing data files.
    * @param input_data_type Input data type ("byte" or "ascii").
    * @return               List of sampled keys.
    */
  def sendSamples(folderPath: String, input_data_type: String): List[Key] = {
    val folder = new File(folderPath)

    // Ensure that the folder exists and is a directory
    assert(folder.exists() && folder.isDirectory, s"$folderPath is not a valid directory")

    val files = folder.listFiles().filter(_.isFile)

    // Ensure that there are files to sample keys from
    assert(files.nonEmpty, "No files found in the specified directory")

    val sampledData = files.flatMap { file =>

      val block = Block.readFromFile(file.getAbsolutePath, input_data_type)
      
      // Define the sampling rate and calculate the maximum number of keys to send
      val samplingRate = 0.05
      val maxKeysToSend = (samplingRate * block.records.size).toInt + 1
      
      // Calculate the maximum size in bytes for the sampled keys
      val maxSizeBytes = 10 * maxKeysToSend
      
      val sampledKeys = Block.sampleKeys(block, maxSizeBytes)
      
      sampledKeys
    }.toList

    // Ensure that sampledData is not empty
    assert(sampledData.nonEmpty, "No keys found after sampling")
    sampledData

  }

  /**
    * Sorts files in the specified folder based on the given partition plan.
    *
    * @param folderPath    Path to the folder containing data files.
    * @param partitionPlan The partition plan for sorting.
    * @param input_data_type Input data type ("byte" or "ascii").
    * @return              List of sorted partitions.
    */
  def sortFiles(folderPath: String, partitionPlan: PartitionPlan, input_data_type: String): List[Partition] = {
    val folder = new File(folderPath)

    // Ensure that the folder exists and is a directory
    assert(folder.exists() && folder.isDirectory, s"$folderPath is not a valid directory")

    val files = folder.listFiles().filter(_.isFile)
    val partitionSortedFiles = files.flatMap { file =>
      // Extract file name and path information
      val fileName = file.getName + "_" + folder.getName
      val path = file.getAbsolutePath
      
      val block = Block.readFromFile(path, input_data_type)
      
      // Sort the block and partition it based on the given partition plan
      val sortedBlock = block.sorted
      Block.partition(sortedBlock, partitionPlan, fileName, input_data_type)
    }.toList

    // Ensure that partitionSortedFiles is not empty
    assert(partitionSortedFiles.nonEmpty, "No partitions found after sorting")

    partitionSortedFiles
  }

  def handleSaveBlockRequest(numWorkers: Int, data_type: String): Unit = {
    // Start a new WorkerSaveBlockThread where the worker will listen on port 9988
    val serverSocket = new ServerSocket(9988)
    logger.info("Worker is listening on port 9988 for SaveBlockRequest")
    val threads = ListBuffer[Thread]()
    val saveThreadResults = ListBuffer[Option[Boolean]]() // List to store results for each thread

    var workerConnected = 0
    val numOtherWorker = numWorkers-1

    while (workerConnected<numOtherWorker) {
      val clientSocket = serverSocket.accept()
      logger.info("New incoming connection")
      val in = new ObjectInputStream(clientSocket.getInputStream)
      val receivedObject = in.readObject
      if (receivedObject.isInstanceOf[SaveBlockRequest]) {
        val saveBlockRequest = receivedObject.asInstanceOf[SaveBlockRequest]
        // create and add to the list a @volatile var saveThreadResult: Option[Boolean] = None for this thread
        val thread = new Thread(new Runnable {
                def run(): Unit = {
                  try {
                    saveBlocksFromWorker(clientSocket, saveBlockRequest, data_type)
                    saveThreadResults += Some(true)
                  } catch {
                    case e: Throwable =>
                      logger.error(s"${e.getMessage}")
                      saveThreadResults += Some(false)
                  }
                }
        })
        threads+=thread
        thread.start()
        workerConnected = workerConnected+1
      } else {
        clientSocket.close()
        logger.error(s"Request received on handleSaveBlock socket but not an instance of SaveBlockRequest")
      }
    }

    serverSocket.close()

    logger.info(s"Started ${threads.size} threads for SaveBlockRequest Requests.")

    // Wait for all threads to finish
    threads.foreach(_.join())

    // Check thread results
    if (saveThreadResults.exists(result => !result.getOrElse(false))) {
      logger.error("Error in one or more threads")
      throw new WorkerTaskError("One or more threads failed in sendSaveBlockRequest")
    } else {
      logger.debug("All threads completed successfully")
    }
  }

  def saveBlocksFromWorker(clientSocket: Socket,saveBlockRequest: SaveBlockRequest, data_type: String): Unit = {
    try {
      
      val clientIP = clientSocket.getInetAddress.getHostAddress
      val clienIPClean = clientIP.replace(".", "")

      var nbBlockToSave = saveBlockRequest.blockToSend
      var blockToSave = Block.fromByteArray(saveBlockRequest.block)

      while (nbBlockToSave != 0) {
        logger.info(s"Worker $clientIP have $nbBlockToSave block to send.")
        val pathToFile = "/tmp/sortnet_TMP/data/tmp/partition_" + clienIPClean + "_" + nbBlockToSave
        Block.writeToFile(blockToSave, pathToFile, data_type)

        val in = new ObjectInputStream(clientSocket.getInputStream)
        val receivedObject = in.readObject

        if (receivedObject.isInstanceOf[SaveBlockRequest]) {
          val lastSaveBlockRequest = receivedObject.asInstanceOf[SaveBlockRequest]
          nbBlockToSave = lastSaveBlockRequest.blockToSend
          blockToSave = Block.fromByteArray(lastSaveBlockRequest.block)
        }
      }
      logger.info(s"Worker $clientIP dont have block left to send.")
      
    
    } catch {
      case e: Exception =>
        logger.error(s"${e.getMessage}", e)
        throw e
    }
  }

  def sendSaveBlockRequest(partitionPlan: PartitionPlan, partitionsToSendList: List[Partition], input_data_type: String): Unit = {
    val myIP = InetAddress.getLocalHost.getHostAddress
    logger.debug(s"My IP is: $myIP")
    // Create a mutable list to store threads and results
    var threads = ListBuffer[Thread]()
    val threadResults = ListBuffer[Option[Boolean]]()

    logger.debug("sendSaveBlockRequest(partitionPlan,partitionsToSendList)")
    logger.debug("partitionPlan:")
    logger.debug(partitionPlan)
    logger.debug("partitionsToSendList:")
    logger.debug(partitionsToSendList)

    // Iterate over partitionPlan
    partitionPlan.partitions.foreach {
      case (partitionIP, _) =>
        logger.info(s"Start for IP: $partitionIP")
        // If partition(0) IP is not equal to my IP
        if (partitionIP != myIP) {
          logger.info("Not my IP, let's start a thread")
          val thread = new Thread(new Runnable {
            def run(): Unit = {
              try {
                logger.info(s"Let's open a socket for IP: $partitionIP")
                val socket = new Socket(partitionIP, 9988) // Use the appropriate port number

                try {
                  // Get the partitions to send to for the current partitionIP
                  val partitionsToSend = partitionsToSendList.filter(_.ip == partitionIP)

                  // Set nbFileToSend to the number of matching partitions
                  var nbFileToSend = partitionsToSend.length

                  partitionsToSend.foreach { partitionToSend =>
                    try {
                      // Read the block from the file
                      val block = Block.readFromFile(partitionToSend.pathToBlockFile, input_data_type)

                      // Create a SaveBlockRequest
                      val saveBlockRequest = new SaveBlockRequest(block.toByteArray, nbFileToSend)

                      // Send SaveBlockRequest on the socket
                      val out = new ObjectOutputStream(socket.getOutputStream)
                      out.writeObject(saveBlockRequest)

                      // delete the file send
                      Files.delete(Paths.get(partitionToSend.pathToBlockFile))

                      // Decrement nbFileToSend
                      nbFileToSend -= 1
                    } catch {
                      case e: Throwable =>
                        logger.error(s"${e.getMessage}")
                        throw e
                    }
                  }

                  // Send a SaveBlockRequest with an empty block and 0
                  val emptyBlock = new Block(List.empty) // Adjust this based on your Block implementation
                  val saveBlockRequest = new SaveBlockRequest(emptyBlock.toByteArray, 0)
                  val out = new ObjectOutputStream(socket.getOutputStream)
                  out.writeObject(saveBlockRequest)

                  // Set the thread result to true on successful execution
                  threadResults += Some(true)
                } catch {
                  case e: Throwable =>
                    logger.error(s"${e.getMessage}")
                    // Set the thread result to false on error
                    threadResults += Some(false)
                    throw e
                } finally {
                  socket.close()
                }
              } catch {
                case e: Throwable =>
                  logger.error(s"${e.getMessage}")
                  // Set the thread result to false on error
                  threadResults += Some(false)
                  throw e
              }
            }
          })

          thread.start()
          threads += thread // Add the thread to the list
        }
    }

    // Wait for all threads to finish
    threads.foreach(_.join())

    // Check thread results
    if (threadResults.exists(result => !result.getOrElse(false))) {
      logger.error("Error in one or more threads")
      throw new WorkerTaskError("One or more threads failed in sendSaveBlockRequest")
    } else {
      logger.debug("All threads completed successfully")
    }
  }

  /**
    * Merges files in the specified folder and writes the sorted output to a file.
    *
    * @param folderPath Path to the folder containing data files.
    */
  def mergeFiles(folderPath: String,outputPath: String, input_data_type: String): Unit = {
    var id = 1

    val folder = new File(folderPath)

    // Ensure that the folder exists and is a directory
    assert(folder.exists() && folder.isDirectory, s"$folderPath is not a valid directory")

    var filesToMerge = folder.listFiles().filter(_.isFile)
    
    logger.info("filesTomerge LIST : ")
    filesToMerge.foreach(file => logger.info(s"file.getAbsolutePath"))

    // While filesToMerge is not empty
    while (filesToMerge.nonEmpty) {
      logger.info(s"Iteration $id")
      // fileA = first file of the list filesToMerge
      val fileA = filesToMerge.head
      var blockMin = Block.readFromFile(fileA.getAbsolutePath, input_data_type)
      logger.info(s"Block Min is file : ${fileA.getAbsolutePath} ot type $input_data_type and is size ${blockMin.records.length}")
      Files.delete(Paths.get(fileA.getAbsolutePath))

      // Iterate through the rest of the files
      logger.info(s"Let's Iterate over ${filesToMerge.length} files")
      for (i <- 1 until filesToMerge.length) {
        logger.info(s"subIt $i : blockMin have ${blockMin.records.length} recs")
        val blockB = Block.readFromFile(filesToMerge(i).getAbsolutePath, input_data_type)
        logger.info(s"Block Min is file : ${filesToMerge(i).getAbsolutePath} ot type $input_data_type and is size ${blockB.records.length}")
        Files.delete(Paths.get(filesToMerge(i).getAbsolutePath))

        // Merge the blocks and get the min and max blocks
        val (blockMinNew, blockMaxNew) = Block.minMax(blockMin, blockB)
        logger.info(s"new min max created : ${blockMinNew.records.length} recs and ${blockMaxNew.records.length} recs")

        // Write the max block to a new file only if it contains records
        if (blockMaxNew.records.nonEmpty) {
          val maxFilePath = folderPath + s"/maxBlock_$i"
          Block.writeToFile(blockMaxNew, maxFilePath, input_data_type)
          logger.info(s"BlockMax write at : $maxFilePath")
        }

        // Update the blockMin for the next iteration
        blockMin = blockMinNew
      }

      // Write the final min block to the output file
      val finalMinFilePath = outputPath + s"/partition.$id"
      Block.writeToFile(blockMin, finalMinFilePath, input_data_type)
      logger.info(s"BlockMin write at : $finalMinFilePath")
      id = id + 1


      // Update the list of files to merge
      filesToMerge = folder.listFiles().filter(_.isFile)
      logger.info("Updated filesTomerge LIST : ")
      filesToMerge.foreach(file => logger.info(s"file.getAbsolutePath"))
    }
  }  
}
