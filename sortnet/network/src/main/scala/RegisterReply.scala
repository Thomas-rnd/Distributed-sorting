package com.cs434.sortnet.network

import java.io.Serializable

class RegisterReply extends Serializable {
  private var message: String = "Hello"

  def getMessage: String = message
}
