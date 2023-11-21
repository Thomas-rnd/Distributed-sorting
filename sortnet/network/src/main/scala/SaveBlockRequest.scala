package com.cs434.sortnet.network

import com.cs434.sortnet.core.Block

import java.io.Serializable
@SerialVersionUID(6261933779652783196L)
class SaveBlockRequest(val block: Array[Byte], var blockToSend: Int) extends Serializable
