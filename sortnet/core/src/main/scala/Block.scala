package com.cs434.sortnet.core

import java.io.{DataInputStream, DataOutputStream, FileInputStream, FileOutputStream, IOException, File, Serializable}
import scala.util.Random
import scala.io.Source
import java.io.{File, PrintWriter}

/**
 * Represents a block of records.
 *
 * @param records The list of records (key and value) in the block.
 */
case class Block(records: List[Record]) extends Serializable {
  /**
   * Sorts the records based on the key.
   *
   * @return A new Block with sorted records.
   */
  def sorted: Block = Block(records.sortBy(_.key))

  /**
   * Serializes the Block to a byte array.
   *
   * @return The serialized byte array.
   */
  def toByteArray: Array[Byte] = {
    try {
      val dataSize = records.map(_.toByteArray.length).sum
      val byteArrayOutputStream = new java.io.ByteArrayOutputStream(dataSize)
      val dataOutputStream = new DataOutputStream(byteArrayOutputStream)

      for (record <- records) {
        dataOutputStream.write(record.toByteArray)
      }

      // Ensure that the serialized byte array has the expected size
      assert(byteArrayOutputStream.size == dataSize, s"Serialized data size mismatch: expected $dataSize," +
        s" actual ${byteArrayOutputStream.size}")

      byteArrayOutputStream.toByteArray
    } catch {
      case e: IOException =>
        throw new RuntimeException("Error while serializing Block", e)
    }
  }
}

/**
 * Companion object for Block, providing utility functions.
 */
@SerialVersionUID(7061933779652783196L)
object Block extends Serializable {

  /**
   * Deserializes a Block from a byte array.
   *
   * @param bytes The byte array to deserialize.
   * @return A new Block instance.
   */
  def fromByteArray(bytes: Array[Byte]): Block = {
    val dataInputStream = new DataInputStream(new java.io.ByteArrayInputStream(bytes))

    // Ensure that the byte array size is a multiple of the record size
    assert(bytes.length % Record.recordSize == 0, s"Invalid byte array size for deserialization: ${bytes.length}")

    val numRecords = bytes.length / Record.recordSize

    val records = (1 to numRecords).map { _ =>
      Record.fromByteArray(dataInputStream)
    }.toList

    Block(records)
  }

  /**
   * Reads a Block from a binary file.
   *
   * @param filePath The path to the binary file.
   * @return A new Block instance.
   */
  def readFromByteFile(filePath: String): Block = {
    val dataInputStream = new DataInputStream(new FileInputStream(filePath))

    try {
      val bytes = new Array[Byte](dataInputStream.available())
      dataInputStream.readFully(bytes)
      val block = Block.fromByteArray(bytes)

      // Add assertions to check key length
      block.records.foreach { record =>
        assert(record.key.bytes.length == Key.keySize, s"Invalid key length for record in file: $filePath")
      }

      block
    } finally {
      dataInputStream.close()
    }
  }

  /**
   * Reads a Block from an ASCII file.
   *
   * @param filePath The path to the ASCII file.
   * @return A new Block instance.
   */
  def readFromASCIIFile(filePath: String): Block = {
    val lines = Source.fromFile(filePath).getLines().toList
    val validRecords = lines.flatMap { line =>
      if (line.length >= 10) {
        val keyStr = line.take(10)
        val dataStr = line.drop(10) + "\r\n"

        val keyBytes = keyStr.getBytes("UTF-8")
        val dataBytes = dataStr.getBytes("UTF-8")
        // Assertions to check the sizes of key and value
        assert(keyBytes.length == Key.keySize, s"The key read from the file has the wrong size: expected " +
          s"${Key.keySize}, actual ${keyBytes.length}")
        assert(dataBytes.length == Value.valueSize, s"The value read from the file has the wrong size: expected" +
          s"${Value.valueSize}, actual ${dataBytes.length}")

        Some(Record(Key(keyBytes), Value(dataBytes)))
      } else {
        None
      }
    }
    Block(validRecords)
  }

  /**
   * Reads a Block from a file.
   *
   * @param filePath The path to the binary file.
   * @param input_data_type Input data type ("byte" or "ascii").
   * @return A new Block instance.
   */
  def readFromFile(filePath: String, input_data_type: String): Block = {
    if (input_data_type == "ascii") {
      readFromASCIIFile(filePath)
    } else if (input_data_type == "byte") {
      readFromByteFile(filePath)
    } else {
      throw new IllegalArgumentException("Invalid input_data_type. Must be 'byte' or 'ascii'.")
    }
  }

  /**
   * Writes a Block to a binary file.
   *
   * @param block The Block to write.
   * @param filePath The path to the binary file.
   */
  def writeToByteFile(block: Block, filePath: String): Unit = {
    val file = new File(filePath)
    val dataOutputStream = new DataOutputStream(new FileOutputStream(file))

    try {
      val byteArray = block.toByteArray
      dataOutputStream.write(byteArray)
    } finally {
      dataOutputStream.close()
    }
  }

  /**
   * Writes a Block to an ASCII file.
   *
   * @param block The Block to write.
   * @param filePath The path to the ASCII file.
   * @return A new Block instance.
   */
  def writeToASCIIFile(block: Block, filePath: String): Unit = {
    val writer = new PrintWriter(new File(filePath))

    try {
      block.records.foreach { record =>
        val keyStr = new String(record.key.bytes, "UTF-8")
        val dataStr = new String(record.value.bytes, "UTF-8")
        assert(keyStr.getBytes("UTF-8").length == Key.keySize, s"The key written to the file has the wrong size: expected " +
          s"${Key.keySize}, actual ${keyStr.getBytes("UTF-8").length}")
        assert(dataStr.getBytes("UTF-8").length == Value.valueSize, s"The value written to the file has the wrong size: expected " +
          s"${Value.valueSize}, actual ${dataStr.getBytes("UTF-8").length}")

        writer.print(keyStr + dataStr)
      }
    } catch {
      case e: Exception =>
        // Handle any exceptions that may occur during writing
        throw new RuntimeException(s"Error while writing Block to file: $filePath", e)
    } finally {
      // Close the writer to release system resources
      writer.close()
    }
  }

  /**
   * Writes a Block to a file.
   *
   * @param block The Block to write.
   * @param data_type Input data type ("byte" or "ascii").
   * @param filePath The path to the binary file.
   */
  def writeToFile(block: Block, filePath: String, data_type: String): Unit = {
    if (data_type == "ascii") {
      writeToASCIIFile(block, filePath)
    } else if (data_type == "byte") {
      writeToByteFile(block, filePath)
    } else {
      throw new IllegalArgumentException("Invalid data_type. Must be 'byte' or 'ascii'.")
    }
  }

  /**
   * Partitions the Block based on a PartitionPlan.
   *
   * @param block The Block to partition.
   * @param plan The PartitionPlan for partitioning.
   * @param nameFile The name of the file being partitioned.
   * @return A list of Partition instances.
   */
  def partition(block: Block, plan: PartitionPlan, nameFile: String, input_data_type: String): List[Partition] = {
    var indexPartition = 0
    plan.partitions.toList.map { case (ip, keyRange) =>
      val startKey = keyRange.startKey
      val endKey = keyRange.endKey

      val filteredRecords = block.records.filter(record =>
        (startKey <= record.key) && (record.key <= endKey)
      )

      val pathToPartition = "/tmp/sortnet_TMP/data/tmp/" + nameFile + "_" + indexPartition
      writeToFile(Block(filteredRecords), pathToPartition, input_data_type)

      indexPartition = indexPartition + 1
      Partition(ip, pathToPartition)
    }
  }

  /**
   * Samples keys from the Block.
   *
   * @param block The Block to sample keys from.
   * @param maxSizeBytes The maximum size in bytes for the sampled keys.
   * @return A list of sampled keys.
   */
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

    // Assertions to ensure that the sampled keys meet expectations
    assert(sampledKeys.length > 0, "No keys have been sampled keys from block")
    assert(currentSize <= maxSizeBytes, "Sampled keys exceed the specified maximum size")

    sampledKeys
  }

  /**
   * Merge and Sort 2 given blocks to return 2 sorted blocks.
   *
   * @param blockA The BlockA to merge and sort.
   * @param blockB The BlockB to merge and sort.
   * @return A tuple of blockMin and blockMax.
   */
  def minMax(blockA: Block, blockB: Block): (Block, Block) = {
    // Combine records from both blocks
    val recordsList = blockA.records ++ blockB.records

    // Sort the combined records by key
    val sortedRecords = recordsList.sortBy(_.key)

    // Split the sorted records into two blocks
    val blockSize = sortedRecords.length
    val nb_record_per_file = 335544//104857 for 10 MB - 335544 for 32MB
    val blockMinRecords = sortedRecords.take(nb_record_per_file) // Adjust the number of records as needed
    val blockMaxRecords = sortedRecords.drop(nb_record_per_file)

    // Create Block instances for blockMin and blockMax
    val blockMin = Block(blockMinRecords)
    val blockMax = Block(blockMaxRecords)

    // Assert that the sum of records in input blocks equals the sum in output blocks
    assert(blockA.records.length + blockB.records.length == blockMin.records.length + blockMax.records.length, "Sum of records in input blocks is not equal to sum in output blocks")

    // Return the tuple of blockMin and blockMax
    (blockMin, blockMax)
  }
}