package com.cs434.sortnet.network

import java.io.Serializable

class SamplingKeyReply(samplingKeys: List[String]) extends Serializable {
  def getSamplingKeys: List[String] = samplingKeys
}
