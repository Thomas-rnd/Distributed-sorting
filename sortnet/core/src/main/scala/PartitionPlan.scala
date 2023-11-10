package com.cs434.sortnet.core

import java.io.Serializable

// Define the PartitionPlan case class
case class PartitionPlan(partitions: List[(String, KeyRange)]) extends Serializable {
  override def toString: String = {
    val partitionStrings = partitions.map {
      case (name, keyRange) =>
        s"$name: $keyRange"
    }
    partitionStrings.mkString(", ")
  }
}
