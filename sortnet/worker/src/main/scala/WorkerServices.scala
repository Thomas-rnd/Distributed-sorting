package com.cs434.sortnet.worker

import java.io._
import java.util.concurrent.{ExecutorService, Executors}
import java.net.{ServerSocket, Socket, InetAddress}
import java.nio.file.{Files, Paths}
import scala.collection.mutable.{Map, HashMap, ListBuffer}

import com.cs434.sortnet.network._
import com.cs434.sortnet.core._

object WorkerServices {

  /** Sends sampled keys from the specified folder.
    *
    * @param folderPath
    *   Path to the folder containing data files.
    * @return
    *   List of sampled keys.
    */
  def sendSamples(folderPath: String): List[Key] = {
    val folder = new File(folderPath)

    // Ensure that the folder exists and is a directory
    assert(
      folder.exists() && folder.isDirectory,
      s"$folderPath is not a valid directory"
    )

    val files = folder.listFiles().filter(_.isFile)

    // Ensure that there are files to sample keys from
    assert(files.nonEmpty, "No files found in the specified directory")

    val sampledData = files.flatMap { file =>
      val block = Block.readFromASCIIFile(file.getAbsolutePath)

      // Define the sampling rate and calculate the maximum number of keys to send
      val samplingRate = 0.1
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

  /** Sorts files in the specified folder based on the given partition plan.
    *
    * @param folderPath
    *   Path to the folder containing data files.
    * @param partitionPlan
    *   The partition plan for sorting.
    * @return
    *   List of sorted partitions.
    */
  def sortFiles(
      folderPath: String,
      partitionPlan: PartitionPlan
  ): List[Partition] = {
    val folder = new File(folderPath)

    // Ensure that the folder exists and is a directory
    assert(
      folder.exists() && folder.isDirectory,
      s"$folderPath is not a valid directory"
    )

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
    println("Worker is listening on port 9988 for SaveBlockRequest")
    val threads = ListBuffer[Thread]()

    var workerConnected = 0
    val numOtherWorker = numWorkers - 1

    while (workerConnected < numOtherWorker) {
      val clientSocket = serverSocket.accept()
      println("New incoming connection")
      val in = new ObjectInputStream(clientSocket.getInputStream)
      val receivedObject = in.readObject
      if (receivedObject.isInstanceOf[SaveBlockRequest]) {
        val saveBlockRequest = receivedObject.asInstanceOf[SaveBlockRequest]
        val thread = new Thread(new Runnable {
          def run(): Unit = {
            saveBlocksFromWorker(clientSocket, saveBlockRequest)
          }
        })
        threads += thread
        thread.start()
        workerConnected = workerConnected + 1
      } else {
        clientSocket.close()
      }

    }

    serverSocket.close()

    println(s"Started ${threads.size} threads for SaveBlockRequest Requests.")

    // Wait for all the threads to finish
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
  }

  def saveBlocksFromWorker(
      clientSocket: Socket,
      saveBlockRequest: SaveBlockRequest
  ): Unit = {
    try {

      val clientIP = clientSocket.getInetAddress.getHostAddress
      val clienIPClean = clientIP.replace(".", "")

      var nbBlockToSave = saveBlockRequest.blockToSend
      var blockToSave = saveBlockRequest.block

      while (nbBlockToSave != 0) {
        println(s"Worker $clientIP have $nbBlockToSave block to send.")
        val pathToFile =
          "/home/red/data/tmp/partition_" + clienIPClean + "_" + nbBlockToSave
        Block.writeToASCIIFile(blockToSave, pathToFile)

        val in = new ObjectInputStream(clientSocket.getInputStream)
        val receivedObject = in.readObject

        if (receivedObject.isInstanceOf[SaveBlockRequest]) {
          val lastSaveBlockRequest =
            receivedObject.asInstanceOf[SaveBlockRequest]
          nbBlockToSave = lastSaveBlockRequest.blockToSend
          blockToSave = lastSaveBlockRequest.block
        }
      }
      println(s"Worker $clientIP dont have block left to send.")

    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
  }

  def sendSaveBlockRequest(
      partitionPlan: PartitionPlan,
      partitionsToSendList: List[Partition]
  ): Unit = {
    val myIP = InetAddress.getLocalHost.getHostAddress
    println(s"Mu IP is : $myIP")
    // Create a mutable list to store threads
    var threads = List[Thread]()

    println("sendSaveBlockRequest(partitionPlan,partitionsToSendList)")
    println("partitionPlan:")
    println(partitionPlan)
    println("partitionsToSendList:")
    println(partitionsToSendList)
    // Iterate over partitionPlan
    partitionPlan.partitions.foreach { case (partitionIP, _) =>
      println(s"Start for IP: $partitionIP")
      // If partition(0) IP is not equal to my IP
      if (partitionIP != myIP) {
        println("Not my ip, let's start a thread")
        val thread = new Thread(new Runnable {
          def run(): Unit = {
            println(s"Let's open a socket for IP: $partitionIP")
            val socket =
              new Socket(partitionIP, 9988) // Use the appropriate port number

            try {
              // Get the partitions to send to for the current partitionIP
              val partitionsToSend =
                partitionsToSendList.filter(_.ip == partitionIP)

              // Set nbFileToSend to the number of matching partitions
              var nbFileToSend = partitionsToSend.length

              partitionsToSend.foreach { partitionToSend =>
                // Read the block from the file
                val block =
                  Block.readFromASCIIFile(partitionToSend.pathToBlockFile)

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
              val emptyBlock =
                new Block(
                  List.empty
                ) // Adjust this based on your Block implementation
              val saveBlockRequest = new SaveBlockRequest(emptyBlock, 0)
              val out = new ObjectOutputStream(socket.getOutputStream)
              out.writeObject(saveBlockRequest)
            } catch {
              case e: Exception =>
                e.printStackTrace()
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
}
