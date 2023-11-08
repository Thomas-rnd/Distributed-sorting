package com.cs434.sortnet.core

// Define the PartitionPlan case class
case class PartitionPlan(workers: List[(WorkerMetadata, Option[KeyRange])])

