object Alex {
/*
  def main(args: Array[String]): Unit = {
  
  
  // Test reading a file and creating a Block instance
  val inputFile = "/home/red/data/input/partition1"
  val block = Block.readFromASCIIFile(inputFile)
  logger.info("Block created from file:")
  logger.info(block)

  logger.info("\nBlock Key\n")
  block.records.foreach(record => logger.info(record.key.toStringAsIntArray))

  val updatedBlock = block.sorted
  logger.info("\nSortedBlock Key\n")
  updatedBlock.records.foreach(record => logger.info(record.key.toStringAsIntArray))

  // Test writing the updated Block to a file
  val outputFile = "/home/red/data/input/partition2COPY"
  Block.writeToASCIIFile(updatedBlock, outputFile)
  logger.info(s"Updated Block written to $outputFile")
/*
  // Optionally, you can read the written file back to check if it matches the original block
  val recreatedBlock = Block.readFromASCIIFile(outputFile)
  logger.info("Block recreated from the written file:")
  logger.info(recreatedBlock)

  val keyBytes1: Array[Byte] = Array(0, 0, 0, 0, 0, 0, 0, 0, 0, 0) // Example byte array
  val keyBytes2: Array[Byte] = Array(0, 0, 0, 0, 0, 0, 0, 0, 0, 1)
  val keyBytes3: Array[Byte] = Array(0, 0, 0, 0, 0, 0, 0, 0, 0, 2)
  val keyBytes4: Array[Byte] = Array(0, 0, 0, 0, 0, 0, 0, 0, 0, 3)
  val keyBytes5: Array[Byte] = Array(0, 0, 0, 0, 0, 0, 0, 0, 0, 4)
  val keyBytes6: Array[Byte] = Array(0, 0, 0, 0, 0, 0, 0, 0, 0, 5)
  val keyBytes7: Array[Byte] = Array(127, 127, 127, 127, 127, 127, 127, 127, 127, 127)
  val k1: Key = Key(keyBytes1)
  val k2: Key = Key(keyBytes2)
  val k3: Key = Key(keyBytes3)
  val k4: Key = Key(keyBytes4)
  val k5: Key = Key(keyBytes5)
  val k6: Key = Key(keyBytes6)
  val k7: Key = Key(keyBytes7)

  logger.info(k1.toStringAsIntArray)
  logger.info(k2.toStringAsIntArray)
  logger.info(k3.toStringAsIntArray)
  logger.info(k4.toStringAsIntArray)
  logger.info(k5.toStringAsIntArray)
  logger.info(k6.toStringAsIntArray)

  val keyList: List[Key] = List(k1, k7, k5, k4, k2, k6, k3)

  keyList.foreach(key => logger.info(key.toStringAsIntArray))

  val sortedKeyList = keyList.sorted

  sortedKeyList.foreach(key => logger.info(key.toStringAsIntArray))

*/

  }*/
}