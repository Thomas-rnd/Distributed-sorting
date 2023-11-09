package com.cs434.sortnet.network

import java.io.Serializable

class SamplingKeyRequest extends Serializable {
  private var message: String = "samplingKeyRequest"

  def getMessage: String = message
}
