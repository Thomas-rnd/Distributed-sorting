package com.cs434.sortnet.network

import java.io.Serializable
@SerialVersionUID(6961933779652783196L)
class TerminateRequest(val success: Boolean, val reason: String) extends Serializable {}