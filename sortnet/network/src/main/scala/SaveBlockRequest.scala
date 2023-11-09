package com.cs434.sortnet.network

import com.cs434.sortnet.core.Block

import java.io.Serializable

class SaveBlockRequest(val block: Block, var otherBlockToSend: Boolean) extends Serializable
