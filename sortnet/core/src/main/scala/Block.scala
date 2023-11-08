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

  // Partition the Block based on a PartitionPlan
  def partition(block: Block, plan: PartitionPlan): List[Partition] = {
    plan.workers.map {
        case (worker, Some(keyRange)) =>
        val filteredRecords = block.records.filter(record => keyRange.startKey <= record.key && record.key <= keyRange.endKey)
        Partition(worker, Block(filteredRecords))
        case (worker, None) =>
        Partition(worker, block)
    }
  }

  // Sample keys from the Block
  def sampleKeys(block: Block, numSamples: Int): List[Key] = {
    val allKeys = block.records.map(_.key)
    val step = allKeys.length / numSamples
    val sampledKeys = (0 until numSamples).map(i => allKeys(i * step)).toList
    sampledKeys
  }
}
