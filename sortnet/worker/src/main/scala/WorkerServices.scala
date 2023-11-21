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
}
