package com.cs434.sortnet.worker

import java.io._
import java.net._
import java.util.{ ArrayList }
import scala.jdk.CollectionConverters

import com.cs434.sortnet.network._
import com.cs434.sortnet.core._

import org.apache.logging.log4j.scala.Logging

object Alex {

  def main(args: Array[String]): Unit = {
  
  
  // Test reading a file and creating a Block instance
  val inputFile = "/home/red/data/input/partition2"
  val block = Block.readFromFile(inputFile,"ascii")
  println("Block created from file:")
  //println(block)

  println("\nBlock Key\n")
  block.records.foreach(record => println(record.key.toStringAsIntArray))

  val updatedBlock = block.sorted
  println("\nSortedBlock Key\n")
  updatedBlock.records.foreach(record => println(record.key.toStringAsIntArray))

  // Test writing the updated Block to a file
  val outputFile = "/home/red/data/input/partition2COPY"
  //Block.writeToByteFile(block, outputFile)
  Block.writeToFile(updatedBlock, outputFile,"ascii")
  //println(s"Updated Block written to $outputFile")

  }
}