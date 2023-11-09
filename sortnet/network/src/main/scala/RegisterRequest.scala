package com.cs434.sortnet.network

import java.io.Serializable

class RegisterRequest extends Serializable {
  private var message: String = "register"

  def getMessage: String = message
}
