package com.cs434.sortnet.core

import java.io.Serializable

// Define the PartitionPlan case class
case class PartitionPlan(partitions: List[(String, KeyRange)]) extends Serializable
