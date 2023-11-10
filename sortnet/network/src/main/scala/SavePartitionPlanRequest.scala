package com.cs434.sortnet.network

import java.io.Serializable
import com.cs434.sortnet.core.PartitionPlan

case class SavePartitionPlanRequest(partitionPlan: PartitionPlan) extends Serializable
