package com.cs434.sortnet.worker

import java.io._
import java.util.concurrent.{ExecutorService, Executors}
import java.net.{ServerSocket, Socket, InetAddress}
import java.nio.file.{Files, Paths}
import scala.collection.mutable.{Map, HashMap, ListBuffer}

import com.cs434.sortnet.network._
import com.cs434.sortnet.core._

object WorkerServices {

  def sendSamples(folderPath: String): List[Key] = {
    val folder = new File(folderPath)

    if (folder.exists() && folder.isDirectory) {
      val files = folder.listFiles().filter(_.isFile)
      val sampledData = files.flatMap { file =>
        val block = Block.readBlockFromASCIIFile(file.getAbsolutePath)
        val samplingRate = 0.1
        // if file to small we still send 1 key
        val maxKeysToSend = (samplingRate * block.records.size).toInt + 1
        // 10 bytes by key
        val maxSizeBytes = 10 * maxKeysToSend
        val sampledKeys = Block.sampleKeys(block, maxSizeBytes)
        // println(s"Sampled Keys: $sampledKeys")
        sampledKeys
      }.toList

      sampledData

    } else {
      throw new IllegalArgumentException(
        s"$folderPath is not a valid directory"
      )
    }
  }
}
