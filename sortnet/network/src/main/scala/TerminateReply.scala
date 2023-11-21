package com.cs434.sortnet.network

import java.io.Serializable
@SerialVersionUID(6861933779652783196L)
class TerminateReply(val success: Boolean) extends Serializable {}