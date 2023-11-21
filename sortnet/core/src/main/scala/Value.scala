package com.cs434.sortnet.core

import java.io.{DataInputStream, IOException, Serializable}

/**
 * Represents a value in a key-value pair.
 *
 * @param bytes The byte array representing the value.
 */
@SerialVersionUID(7461933779652783196L)
case class Value(bytes: Array[Byte]) extends Serializable {

  /**
   * Serializes the value to a byte array.
   *
   * @return The serialized byte array.
   */
  def toByteArray: Array[Byte] = {
    try {
      // Create a new byte array to hold the value
      val byteArray = new Array[Byte](bytes.length)

      System.arraycopy(bytes, 0, byteArray, 0, bytes.length)

      byteArray
    } catch {
      case e: IOException =>
        throw new RuntimeException("Error while serializing Value", e)
    }
  }

  /**
   * Returns a string representation of the value.
   *
   * @return The string representation of the value.
   */
  override def toString: String = {
    val byteArrayAsString = new String(bytes, "UTF-8")
    s"Value([$byteArrayAsString])"
  }
}

/**
 * Companion object for the Value class.
 */
object Value {
  // Size of a value in bytes
  val valueSize = 90

  /**
   * Deserializes a Value from a DataInputStream.
   *
   * @param dataInputStream The DataInputStream containing the serialized value.
   * @return A new Value instance.
   */
  def fromByteArray(dataInputStream: DataInputStream): Value = {
    // Read value bytes from the DataInputStream
    val valueBytes = new Array[Byte](valueSize)
    dataInputStream.readFully(valueBytes)

    Value(valueBytes)
  }
}
