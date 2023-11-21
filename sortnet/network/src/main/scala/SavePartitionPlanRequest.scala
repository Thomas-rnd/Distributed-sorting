package com.cs434.sortnet.network

import java.io.Serializable
import com.cs434.sortnet.core.PartitionPlan

@SerialVersionUID(5461933779652783196L)
case class SavePartitionPlanRequest(partitionPlan: PartitionPlan) extends Serializable
