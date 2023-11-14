package com.cs434.sortnet.core

import java.io.{DataInputStream, DataOutputStream, IOException}

@SerialVersionUID(7461933779652783196L)
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

  override def toString: String = {
    val byteArrayAsString = new String(bytes, "UTF-8")
    s"Value([$byteArrayAsString])"
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
