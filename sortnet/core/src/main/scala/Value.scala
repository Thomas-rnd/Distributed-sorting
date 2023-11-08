package com.cs434.sortnet.core

import java.io.{DataInputStream, DataOutputStream, IOException}

// Define the Value class
case class Value(bytes: Array[Byte]) extends Serializable {
  // Serialize the Value to a byte array
  def toByteArray: Array[Byte] = {
    try {
      val byteArray = new Array[Byte](bytes.length)
      System.arraycopy(bytes, 0, byteArray, 0, bytes.length)
      byteArray
    } catch {
      case e: IOException =>
        throw new RuntimeException("Error while serializing Value", e)
    }
  }
}

// Companion object for Value
object Value {
  val valueSize = 90 // The size of the value in bytes

  // Deserialize a Value from a DataInputStream
  def fromByteArray(dataInputStream: DataInputStream): Value = {
    val valueBytes = new Array[Byte](valueSize)
    dataInputStream.readFully(valueBytes)
    Value(valueBytes)
  }
}
