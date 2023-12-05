package com.cs434.sortnet.core

import java.io.{DataInputStream, IOException, Serializable}

/**
 * Represents a value in a key-value pair.
 *
 * @param bytes The byte array representing the value.
 */
@SerialVersionUID(7461933779652783196L)
case class Value(bytes: Array[Byte]) extends Serializable {

  def toUnsignedByte(byteValue: Byte): Int = {
    byteValue.toInt & 0xFF
  }

  /**
   * Compares this value with another value.
   *
   * @param that The other value to compare with.
   * @return A negative integer, zero, or a positive integer as this value is less than, equal to,
   *         or greater than the specified value.
   */
  def compare(that: Value): Int = {
    val thisBytes = this.bytes
    val thatBytes = that.bytes
    val length = thisBytes.length min thatBytes.length

    var i = 0
    while (i < length) {
      val cmp = toUnsignedByte(thisBytes(i)) - toUnsignedByte(thatBytes(i))
      if (cmp != 0) return cmp
      i += 1
    }

    // If the common prefix is equal, compare based on the length
    thisBytes.length - thatBytes.length
  }


  /**
   * Serializes the value to a byte array.
   *
   * @return The serialized byte array.
   */
  def toByteArray: Array[Byte] = {
    try {
      // Create a new byte array to hold the value
      val byteArray = new Array[Byte](bytes.length)
      Array.copy(bytes, 0, byteArray, 0, bytes.length)
      assert(java.util.Arrays.equals(byteArray, bytes), "Array copy failed")
      byteArray
    } catch {
      case e: IOException =>
        throw new RuntimeException("Error while serializing Value", e)
    }
  }

  /**
   * Returns a string representation of the value.
   *
   * @return The string representation.
   */
  override def toString: String = {
    val intArray = bytes.map(byteValue => byteValue & 0xFF)
    s"Value[${intArray.mkString(" ")}]"
  }
}

/**
 * Companion object for the Value class.
 */
object Value {
  // Size of a value in bytes
  val valueSize = 90

  /**
   * Deserializes a Value from a byte array.
   *
   * @param byteArray The byte array to deserialize.
   * @return A new Value instance.
   */
  def fromByteArray(byteArray: Array[Byte]): Value = Value(byteArray)
}
