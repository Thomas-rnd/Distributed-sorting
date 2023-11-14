package com.cs434.sortnet.core

import java.io.{DataInputStream, DataOutputStream, IOException, File, Serializable}
import scala.util.Random
import scala.io.Source
import java.io.{File, PrintWriter}

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
@SerialVersionUID(7061933779652783196L)
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

  // Write a Block to a file with a newline at the end
  def writeToFile(block: Block, filename: String): Unit = {
    val os = new java.io.FileOutputStream(filename, true)  // 'true' for append mode
    os.write(block.toByteArray)
    os.write("\n".getBytes("UTF-8"))  // append newline
    os.close()
  }


  // Partition the Block based on a PartitionPlan
  def partition(block: Block, plan: PartitionPlan): List[Partition] = {
    plan.partitions.map {
      case (ip, keyRange) =>
            val filteredRecords = block.records.filter(record =>
        (keyRange.startKey <= record.key) && (keyRange.endKey.isEmpty || record.key <= keyRange.endKey.get)
        )
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

  def readBlockFromASCIIFile(filePath: String): Block = {
    val lines = Source.fromFile(filePath).getLines().toList
    val validRecords = lines.flatMap { line =>
      if (line.length >= 10) {
        val keyStr = line.take(10)
        val dataStr = line.drop(10) + "\r\n"
        
        val keyBytes = keyStr.getBytes("UTF-8")
        val dataBytes = dataStr.getBytes("UTF-8")
        assert(keyBytes.length == Key.keySize, "The key read from file has the wrong size: expected " +
          s"${Key.keySize}, actual ${keyBytes.length}")
        assert(dataBytes.length == Value.valueSize, "The value read from file has the wrong size: expected" + 
          s"${Value.valueSize}, actual ${dataBytes.length}")
        
        Some(Record(Key(keyBytes), Value(dataBytes)))
      } else {
        None
      }
    }
    Block(validRecords)
  }

  def writeBlockToASCIIFile(block: Block, filePath: String): Unit = {
    // Create a PrintWriter to write to the specified file
    val writer = new PrintWriter(new File(filePath))

    try {
      // Iterate through the records in the block and write each record to the file
      block.records.foreach { record =>
        // Convert the key and data back to strings
        val keyStr = new String(record.key.bytes, "UTF-8")
        val dataStr = new String(record.value.bytes, "UTF-8")
        assert(keyStr.getBytes("UTF-8").length == Key.keySize, s"The key written to file has the wrong size: expected " + 
        s"${Key.keySize}, actual ${keyStr.getBytes("UTF-8").length}")
        assert(dataStr.getBytes("UTF-8").length == Value.valueSize, s"The value written to file has the wrong size: expected " +
        s"${Value.valueSize}, actual ${dataStr.getBytes("UTF-8").length}")

        // Write the key and data to the file
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

}
