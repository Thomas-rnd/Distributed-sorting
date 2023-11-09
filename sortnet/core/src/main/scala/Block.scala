package com.cs434.sortnet.core

import java.io.{DataInputStream, DataOutputStream, IOException}
import java.io.Serializable

case class Block(records: List[Record]) extends Serializable {
  // Sort the records based on Key
  def sorted: Block = Block(records.sortBy(_.key))

  // Serialize the Block to a byte array
  def toByteArray: Array[Byte] = {
    try {
      val dataSize = records.map(_.toByteArray.length).sum
      val byteArrayOutputStream = new java.io.ByteArrayOutputStream(dataSize)
      val dataOutputStream = new DataOutputStream(byteArrayOutputStream)

      for (record <- records) {
        dataOutputStream.write(record.toByteArray)
      }

      byteArrayOutputStream.toByteArray
    } catch {
      case e: IOException =>
        throw new RuntimeException("Error while serializing Block", e)
    }
  }
}

object Block extends Serializable {

  // Deserialize a Block from a byte array
  def fromByteArray(bytes: Array[Byte]): Block = {
    val dataInputStream = new DataInputStream(new java.io.ByteArrayInputStream(bytes))
    val numRecords = bytes.length / Record.recordSize

    val records = (1 to numRecords).map { _ =>
      Record.fromByteArray(dataInputStream)
    }.toList

    Block(records)
  }

  // Read a Block from a file
  def readFromFile(filename: String): Block = {
    val bytes = scala.io.Source.fromFile(filename, "ISO-8859-1").map(_.toByte).toArray
    fromByteArray(bytes)
  }

  // Write a Block to a file
  def writeToFile(block: Block, filename: String): Unit = {
    val os = new java.io.FileOutputStream(filename)
    os.write(block.toByteArray)
    os.close()
  }

  def createFromFolder(folderPath: String): Block = {
    val folder = new File(folderPath)

    if (folder.exists() && folder.isDirectory) {
      val files = folder.listFiles().filter(_.isFile)
      val allRecords = files.flatMap { file =>
        val block = readFromFile(file.getAbsolutePath)
        block.records
      }.toList

      Block(allRecords)
    } else {
      throw new IllegalArgumentException(s"$folderPath is not a valid directory")
    }
  }

  // Partition the Block based on a PartitionPlan
  def partition(block: Block, plan: PartitionPlan): List[Partition] = {
    plan.partitions.map {
      case (ip, keyRange) =>
        val filteredRecords = block.records.filter(record => keyRange.startKey <= record.key && record.key <= keyRange.endKey)
        //TODO define a way to get unique filePath for exemple ./partition_X_Y with X index on ip on list and Y num of partition for each X
        val pathToFile = "./partitionX"
        //Block(filteredRecords)
        //TODO write this block in disk and store it's path
        Partition(ip, pathToFile)
    }
  }

  // Sample keys from the Block
  def sampleKeys(block: Block, maxSizeBytes: Int): List[Key] = {
    val allKeys = block.records.map(_.key)
    val shuffledKeys = Random.shuffle(allKeys)

    var currentSize = 0
    val sampledKeys = shuffledKeys.takeWhile { key =>
      val keySize = key.toByteArray.length
      if (currentSize + keySize <= maxSizeBytes) {
        currentSize += keySize
        true
      } else {
        false
      }
    }

    sampledKeys
  }
}
