package com.cs434.sortnet.core

/**
 * Represents a partition plan for distributing keys across multiple workers.
 *
 * @param partitions  A list of tuples, each containing the worker's name and associated key range.
 */
@SerialVersionUID(7261933779652783196L)
case class PartitionPlan(partitions: List[(String, KeyRange)]) extends Serializable {

  /**
   * Generates a human-readable string representation of the partition plan.
   *
   * @return A string representation of the partition plan.
   */
  override def toString: String = {
    // Convert each partition tuple to a formatted string and join them with commas
    val partitionStrings = partitions.map {
      case (name, keyRange) =>
        s"$name: $keyRange"
    }
    partitionStrings.mkString(", ")
  }
}
