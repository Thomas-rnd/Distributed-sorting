package com.cs434.sortnet.worker

import java.io._
import java.util.concurrent.{ExecutorService, Executors}
import java.net.{ServerSocket, Socket, InetAddress}
import java.nio.file.{Files, Paths}
import scala.collection.mutable.{Map, HashMap, ListBuffer}

import com.cs434.sortnet.network._
import com.cs434.sortnet.core._

import org.apache.logging.log4j.scala.Logging

object  WorkerServices extends Logging{

 /**
 * Sends sampled keys from the specified folder.
 *
 * @param folderPath Path to the folder containing data files.
 * @return           List of sampled keys.
 */
def sendSamples(folderPath: String): List[Key] = {
  val folder = new File(folderPath)

  // Ensure that the folder exists and is a directory
  assert(folder.exists() && folder.isDirectory, s"$folderPath is not a valid directory")

  val files = folder.listFiles().filter(_.isFile)

  // Ensure that there are files to sample keys from
  assert(files.nonEmpty, "No files found in the specified directory")

  val sampledData = files.flatMap { file =>
    val block = Block.readFromASCIIFile(file.getAbsolutePath)
    
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
 * @return              List of sorted partitions.
 */
def sortFiles(folderPath: String, partitionPlan: PartitionPlan): List[Partition] = {
  val folder = new File(folderPath)

  // Ensure that the folder exists and is a directory
  assert(folder.exists() && folder.isDirectory, s"$folderPath is not a valid directory")

  val files = folder.listFiles().filter(_.isFile)
  val partitionSortedFiles = files.flatMap { file =>
    // Extract file name and path information
    val fileName = file.getName
    val path = file.getAbsolutePath
    
    val block = Block.readFromASCIIFile(path)
    
    // Sort the block and partition it based on the given partition plan
    val sortedBlock = block.sorted
    Block.partition(sortedBlock, partitionPlan, fileName)
  }.toList

  // Ensure that partitionSortedFiles is not empty
  assert(partitionSortedFiles.nonEmpty, "No partitions found after sorting")

  partitionSortedFiles
}


  def handleSaveBlockRequest(numWorkers: Int): Unit = {
    // Start a new WorkerSaveBlockThread where the worker will listen on port 9988
    val serverSocket = new ServerSocket(9988)
    logger.info("Worker is listening on port 9988 for SaveBlockRequest")
    val threads = ListBuffer[Thread]()

    var workerConnected = 0
    val numOtherWorker = numWorkers-1

    while (workerConnected<numOtherWorker) {
      val clientSocket = serverSocket.accept()
      logger.info("New incoming connection")
      val in = new ObjectInputStream(clientSocket.getInputStream)
      val receivedObject = in.readObject
      if (receivedObject.isInstanceOf[SaveBlockRequest]) {
        val saveBlockRequest = receivedObject.asInstanceOf[SaveBlockRequest]
        val thread = new Thread(new Runnable {
                def run(): Unit = {
                  saveBlocksFromWorker(clientSocket,saveBlockRequest)
                }
        })
        threads+=thread
        thread.start()
        workerConnected = workerConnected+1
      } else {
        clientSocket.close()
      }
      
    }

    serverSocket.close()

    logger.info(s"Started ${threads.size} threads for SaveBlockRequest Requests.")

    // Wait for all the threads to finish
    for (thread <- threads) {
        try {
            thread.join()
            logger.info(s"Thread joined: ${thread.getId}")
        } catch {
            case e: InterruptedException =>
                logger.error(s"Thread join interrupted: ${e.getMessage}", e)
        }
    }
  }

  def saveBlocksFromWorker(clientSocket: Socket,saveBlockRequest: SaveBlockRequest): Unit = {
    try {
      
      val clientIP = clientSocket.getInetAddress.getHostAddress
      val clienIPClean = clientIP.replace(".", "")

      var nbBlockToSave = saveBlockRequest.blockToSend
      var blockToSave = saveBlockRequest.block

      while (nbBlockToSave != 0) {
        logger.info(s"Worker $clientIP have $nbBlockToSave block to send.")
        val pathToFile = "/home/red/data/tmp/partition_" + clienIPClean + "_" + nbBlockToSave
        Block.writeToASCIIFile(blockToSave, pathToFile)

        val in = new ObjectInputStream(clientSocket.getInputStream)
        val receivedObject = in.readObject

        if (receivedObject.isInstanceOf[SaveBlockRequest]) {
          val lastSaveBlockRequest = receivedObject.asInstanceOf[SaveBlockRequest]
          nbBlockToSave = lastSaveBlockRequest.blockToSend
          blockToSave = lastSaveBlockRequest.block
        }
      }
      logger.info(s"Worker $clientIP dont have block left to send.")
      
    
    } catch {
      case e: Exception =>
        logger.error(s"${e.getMessage}", e)
    }
  }

  def sendSaveBlockRequest(partitionPlan: PartitionPlan, partitionsToSendList: List[Partition]): Unit = {
    val myIP = InetAddress.getLocalHost.getHostAddress
    logger.info(s"My IP is : $myIP")
    // Create a mutable list to store threads
    var threads = List[Thread]()

    logger.info("sendSaveBlockRequest(partitionPlan,partitionsToSendList)")
    logger.info("partitionPlan:")
    logger.info(partitionPlan)
    logger.info("partitionsToSendList:")
    logger.info(partitionsToSendList)
    // Iterate over partitionPlan
    partitionPlan.partitions.foreach {
      case (partitionIP, _) =>
        logger.info(s"Start for IP: $partitionIP")
        // If partition(0) IP is not equal to my IP
        if (partitionIP != myIP) {
          logger.info("Not my ip, let's start a thread")
          val thread = new Thread(new Runnable {
            def run(): Unit = {
              logger.info(s"Let's open a socket for IP: $partitionIP")
              val socket = new Socket(partitionIP, 9988) // Use the appropriate port number

              try {
                // Get the partitions to send to for the current partitionIP
                val partitionsToSend = partitionsToSendList.filter(_.ip == partitionIP)

                // Set nbFileToSend to the number of matching partitions
                var nbFileToSend = partitionsToSend.length

                partitionsToSend.foreach { partitionToSend =>
                  // Read the block from the file
                  val block = Block.readFromASCIIFile(partitionToSend.pathToBlockFile)

                  // Create a SaveBlockRequest
                  val saveBlockRequest = new SaveBlockRequest(block, nbFileToSend)

                  // Send SaveBlockRequest on the socket
                  val out = new ObjectOutputStream(socket.getOutputStream)
                  out.writeObject(saveBlockRequest)

                  // delete the file send
                  Files.delete(Paths.get(partitionToSend.pathToBlockFile))

                  // Decrement nbFileToSend
                  nbFileToSend -= 1
                }

                // Send a SaveBlockRequest with an empty block and 0
                val emptyBlock = new Block(List.empty) // Adjust this based on your Block implementation
                val saveBlockRequest = new SaveBlockRequest(emptyBlock, 0)
                val out = new ObjectOutputStream(socket.getOutputStream)
                out.writeObject(saveBlockRequest)
              } catch {
                case e: Exception =>
                logger.error(s"${e.getMessage}", e)
              } finally {
                socket.close()
              }
            }
          })

          thread.start()
          threads ::= thread // Add the thread to the list
        }
    }

    // Wait for all threads to finish
    threads.foreach(_.join())
  }

  /**
  * Merges files in the specified folder and writes the sorted output to a file.
  *
  * @param folderPath Path to the folder containing data files.
  */
  /*def mergeFiles(folderPath: String): Unit = {
    val folder = new File(folderPath)

    // Ensure that the folder exists and is a directory
    assert(folder.exists() && folder.isDirectory, s"$folderPath is not a valid directory")

    val files = folder.listFiles().filter(_.isFile)

    // Ensure that there are files to merge
    assert(files.nonEmpty, "No files found in the specified directory")

    val mergedRecords = files.flatMap { file =>
      val path = file.getAbsolutePath
      
      val records = Block.readFromASCIIFile(path).records
      
      // Delete the file after reading its records
      Files.delete(Paths.get(path))
      
      records
    }.toList

    // Define the output file path
    val outputFile = "/home/red/data/output/partition.1"

    // Ensure that there are records to merge and sort
    assert(mergedRecords.nonEmpty, "No records found to merge")
    
    // Sort the merged records and write them to the output file
    val sortedOutput = Block(mergedRecords).sorted
    Block.writeToASCIIFile(sortedOutput, outputFile)
  }*/

  /**
  * Merges files in the specified folder and writes the sorted output to a file.
  *
  * @param folderPath Path to the folder containing data files.
  */
  def mergeFiles(folderPath: String): Unit = {
    val outputPath ="/home/red/data/output"
    var id = 1

    val folder = new File(folderPath)

    // Ensure that the folder exists and is a directory
    assert(folder.exists() && folder.isDirectory, s"$folderPath is not a valid directory")

    var filesToMerge = folder.listFiles().filter(_.isFile)

    // While filesToMerge is not empty
    while (filesToMerge.nonEmpty) {
      // fileA = first file of the list filesToMerge
      val fileA = filesToMerge.head
      var blockMin = Block.readFromASCIIFile(fileA.getAbsolutePath)
      Files.delete(Paths.get(fileA.getAbsolutePath))

      // Iterate through the rest of the files
      for (i <- 1 until filesToMerge.length) {
        val blockB = Block.readFromASCIIFile(filesToMerge(i).getAbsolutePath)
        Files.delete(Paths.get(filesToMerge(i).getAbsolutePath))

        // Merge the blocks and get the min and max blocks
        val (blockMinNew, blockMaxNew) = Block.minMax(blockMin, blockB)

        // Write the max block to a new file only if it contains records
        if (blockMaxNew.records.nonEmpty) {
          val maxFilePath = folderPath + s"/maxBlock_$i"
          Block.writeToASCIIFile(blockMaxNew, maxFilePath)
        }

        // Update the blockMin for the next iteration
        blockMin = blockMinNew
      }

      // Write the final min block to the output file
      val finalMinFilePath = outputPath + s"/partition.$id"
      Block.writeToASCIIFile(blockMin, finalMinFilePath)
      id = id + 1


      // Update the list of files to merge
      filesToMerge = folder.listFiles().filter(_.isFile)
    }
  }  
}