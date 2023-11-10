package com.cs434.sortnet.network

object MessageType extends Enumeration {
  type MessageType = Value
  val Register = Value("Register")
  val Merge = Value("Merge") 
  val SampleKey = Value("SampleKey") 
  val SaveBlock = Value("SaveBlock") 
  val SavePartitionPlan = Value("SavePartitionPlan") 
  val Shuffle = Value("Shuffle") 
  val Sort = Value("Sort") 
  val Terminate = Value("Terminate")
}
