package com.cs434.sortnet.core

import org.scalatest.funsuite.AnyFunSuite

import java.io.{ByteArrayInputStream, DataInputStream, File}
import scala.util.Random

class BlockTest extends AnyFunSuite {

  test("Serialization and deserialization should work correctly") {
    val random = new Random()
    val originalKey1 = Key(Array[Byte](0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 1.toByte, 2.toByte, 3.toByte))
    val originalValue1 = Value(Array.fill(Value.valueSize)(random.nextInt(256).toByte))
    val originalRecord1 = Record(originalKey1, originalValue1)

    val originalKey2 = Key(Array[Byte](0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 7.toByte, 8.toByte, 9.toByte))
    val originalValue2 = Value(Array.fill(Value.valueSize)(random.nextInt(256).toByte))
    val originalRecord2 = Record(originalKey2, originalValue2)

    val originalBlock = Block(List(originalRecord1, originalRecord2))
    val byteArray = originalBlock.toByteArray

    val deserializedBlock = Block.fromByteArray(originalBlock.toByteArray)

    assert(originalBlock.toString == deserializedBlock.toString)
  }

  test("Block sorting should work correctly") {
    val random = new Random()
    val key1 = Key(Array[Byte](0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 1.toByte, 2.toByte, 3.toByte))
    val value1 = Value(Array.fill(Value.valueSize)(random.nextInt(256).toByte))
    val record1 = Record(key1, value1)

    val key2 = Key(Array[Byte](0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 7.toByte, 8.toByte, 9.toByte))
    val value2 = Value(Array.fill(Value.valueSize)(random.nextInt(256).toByte))
    val record2 = Record(key2, value2)

    val unsortedBlock = Block(List(record2, record1))
    val sortedBlock = unsortedBlock.sorted

    assert(sortedBlock.records == List(record1, record2))
  }

  test("Read and write to byte file should work correctly") {
    val random = new Random()
    val originalKey1 = Key(Array[Byte](0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 1.toByte, 2.toByte, 3.toByte))
    val originalValue1 = Value(Array.fill(Value.valueSize)(random.nextInt(256).toByte))
    val originalRecord1 = Record(originalKey1, originalValue1)

    val originalKey2 = Key(Array[Byte](0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 7.toByte, 8.toByte, 9.toByte))
    val originalValue2 = Value(Array.fill(Value.valueSize)(random.nextInt(256).toByte))
    val originalRecord2 = Record(originalKey2, originalValue2)

    val originalBlock = Block(List(originalRecord1, originalRecord2))

    // Write to a byte file
    val byteFilePath = "test_byte_file.bin"
    Block.writeToByteFile(originalBlock, byteFilePath)

    // Read from the byte file
    val readBlock = Block.readFromByteFile(byteFilePath)

    assert(originalBlock.toString == readBlock.toString)

    // Clean up the file
    new File(byteFilePath).delete()
  }

  test("Read and write to ASCII file should work correctly") {
    val random = new Random()
    val originalKey1 = Key(Array[Byte](0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 1.toByte, 2.toByte, 3.toByte))
    val originalValue1 = Value("  00000000000000000000000000000000  0000222200002222000022220000222200002222000000001111\r\n".getBytes("UTF-8"))
    val originalRecord1 = Record(originalKey1, originalValue1)

    val originalKey2 = Key(Array[Byte](0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 7.toByte, 8.toByte, 9.toByte))
    val originalValue2 = Value("  00000000000000000000000000000001  0000222200002222000022220000222200002222000000001111\r\n".getBytes("UTF-8"))
    val originalRecord2 = Record(originalKey2, originalValue2)

    val originalBlock = Block(List(originalRecord1, originalRecord2))

    // Write to an ASCII file
    val asciiFilePath = "test_ascii_file.txt"
    Block.writeToASCIIFile(originalBlock, asciiFilePath)

    // Read from the ASCII file
    val readBlock = Block.readFromASCIIFile(asciiFilePath)

    assert(originalBlock.toString == readBlock.toString)

    // Clean up the file
    new File(asciiFilePath).delete()
  }

  test("Read and write from/to file with different data types should work correctly") {
    val random = new Random()
    val originalKey1 = Key(Array[Byte](0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 1.toByte, 2.toByte, 3.toByte))
    val originalValue1 = Value("  00000000000000000000000000000000  0000222200002222000022220000222200002222000000001111\r\n".getBytes("UTF-8"))
    val originalRecord1 = Record(originalKey1, originalValue1)

    val originalKey2 = Key(Array[Byte](0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 7.toByte, 8.toByte, 9.toByte))
    val originalValue2 = Value("  00000000000000000000000000000001  0000222200002222000022220000222200002222000000001111\r\n".getBytes("UTF-8"))
    val originalRecord2 = Record(originalKey2, originalValue2)

    val originalBlock = Block(List(originalRecord1, originalRecord2))

    // Write to a byte file
    val byteFilePath = "test_byte_file.bin"
    Block.writeToFile(originalBlock, byteFilePath, "byte")

    // Read from the byte file
    val readBlockFromByteFile = Block.readFromFile(byteFilePath, "byte")

    assert(originalBlock.toString == readBlockFromByteFile.toString)

    // Write to an ASCII file
    val asciiFilePath = "test_ascii_file.txt"
    Block.writeToFile(originalBlock, asciiFilePath, "ascii")

    // Read from the ASCII file
    val readBlockFromASCIIFile = Block.readFromFile(asciiFilePath, "ascii")

    assert(originalBlock.toString == readBlockFromASCIIFile.toString)

    // Clean up the files
    new File(byteFilePath).delete()
    new File(asciiFilePath).delete()
  }

  test("Partitioning should work correctly") {
    // Generate a block with random records
    val random = new Random()
    val records = (1 to 100).map { _ =>
      val randomKey = Array.fill(Key.keySize)(random.nextInt(256).toByte)
      val randomValue = Array.fill(Value.valueSize)(random.nextInt(256).toByte)
      Record(Key(randomKey), Value(randomValue))
    }.toList

    val originalBlock = Block(records)

    // Generate a partition plan
    val partitionPlan = PartitionPlan(Array(
      ("1", KeyRange(Key(Array.fill(10)(0.toByte)), Key(Array.fill(10)(127.toByte)))),
      ("2", KeyRange(Key(Array.fill(9)(127.toByte) :+ 128.toByte), Key(Array.fill(10)(255.toByte))))
    ))
    // Partition the block
    val partitions = Block.partition(originalBlock, partitionPlan, "test_partition", "byte")

    // Read each partition and check that records fall within the expected key range
    partitions.foreach { partition =>
      val partitionBlock = Block.readFromByteFile(partition.pathToBlockFile)

      // Check if all records in the partitionBlock fall within the expected key range for any partition in the partitionPlan
      val isInExpectedRange = partitionBlock.records.forall { record =>
        partitionPlan.partitions.exists {
          case (_, keyRange) => (record.key >= keyRange.startKey && record.key <= keyRange.endKey)
        }
      }

      assert(isInExpectedRange, s"All records in partitionBlock are not in the expected range for any partitions.")
    }

    // Clean up the partition files
    partitions.foreach { partition =>
      new File(partition.pathToBlockFile).delete()
    }
  }

  test("Sampling keys should work correctly") {
    // Generate a block with random records
    val random = new Random()
    val records = (1 to 100).map { _ =>
      val randomKey = Array.fill(Key.keySize)(random.nextInt(256).toByte)
      val randomValue = Array.fill(Value.valueSize)(random.nextInt(256).toByte)
      Record(Key(randomKey), Value(randomValue))
    }.toList

    val originalBlock = Block(records)

    // Sample keys from the block
    val sampledKeys = Block.sampleKeys(originalBlock, 1000)

    // Check that sampled keys are within the original block's keys
    sampledKeys.foreach { sampledKey =>
      assert(originalBlock.records.exists(_.key == sampledKey), "Sampled key not found in original block")
    }
  }

  test("Merging and sorting two blocks should work correctly") {
    // Generate two blocks with random records
    val random = new Random()
    val recordsA = (1 to 50).map { _ =>
      val randomKey = Array.fill(Key.keySize)(random.nextInt(256).toByte)
      val randomValue = Array.fill(Value.valueSize)(random.nextInt(256).toByte)
      Record(Key(randomKey), Value(randomValue))
    }.toList

    val recordsB = (51 to 100).map { _ =>
      val randomKey = Array.fill(Key.keySize)(random.nextInt(256).toByte)
      val randomValue = Array.fill(Value.valueSize)(random.nextInt(256).toByte)
      Record(Key(randomKey), Value(randomValue))
    }.toList

    val blockA = Block(recordsA)
    val blockB = Block(recordsB)

    // Merge and sort the blocks
    val (blockMin, blockMax) = Block.minMax(blockA, blockB)

    // Check that records in blockMin are less than or equal to records in blockMax
    assert(blockMin.records.forall { recordA =>
      blockMax.records.forall { recordB =>
        recordA.key.compare(recordB.key) <= 0
      }
    })
  }
}
