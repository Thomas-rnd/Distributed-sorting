package com.cs434.sortnet.network

import com.cs434.sortnet.core.Key

import java.io.Serializable

class SampleKeyReply(val success: Boolean, val sampledKeys: List[Key]) extends Serializable
