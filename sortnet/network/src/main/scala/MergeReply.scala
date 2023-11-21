package com.cs434.sortnet.network

import java.io.Serializable

@SerialVersionUID(5561933779652783196L)
class MergeReply(val success: Boolean) extends Serializable {}