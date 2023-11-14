package com.cs434.sortnet.core

import java.nio.ByteBuffer
import java.io.{DataInputStream, DataOutputStream, IOException}
import java.util.Arrays

@SerialVersionUID(3403353340572833574L)
case class Key(bytes: Array[Byte]) extends Ordered[Key] with Serializable {
  // Implement the compare method required by Ordered
  def compare(that: Key): Int = {
    val thisBytes = this.bytes
    val thatBytes = that.bytes
    val length = thisBytes.length min thatBytes.length

    var i = 0
    while (i < length) {
      val cmp = thisBytes(i) - thatBytes(i)
      if (cmp != 0) return cmp
      i += 1
    }

    // If the common prefix is equal, compare based on the length
    thisBytes.length - thatBytes.length
  }

  // Serialize the Key to a byte array
  def toByteArray: Array[Byte] = {
    try {
      val byteArray = new Array[Byte](bytes.length)
      System.arraycopy(bytes, 0, byteArray, 0, bytes.length)
      byteArray
    } catch {
      case e: IOException =>
        throw new RuntimeException("Error while serializing Key", e)
    }
  }

  override def toString: String = {
    val byteArrayAsString = new String(bytes, "UTF-8")
    s"Key([$byteArrayAsString])"
  }
  

  // Function to increment the key value by 1
  def incrementByOne: Key = {
    val incrementedBytes = new Array[Byte](bytes.length)
    var carry = 1

    // Start from the end of the byte array (LSB) and work towards the beginning (MSB)
    for (i <- bytes.length - 1 to 0 by -1) {
      val sum = (bytes(i) & 0xFF) + carry
      incrementedBytes(i) = (sum & 0xFF).toByte
      carry = sum >> 8
    }

    // If there's still a carry after the loop, it means we've overflowed, we assume that case never happens
    if (carry != 0) {
      // Handle overflow by creating a new larger byte array and copying the incrementedBytes into it
      val newBytes = new Array[Byte](bytes.length + 1)
      System.arraycopy(incrementedBytes, 0, newBytes, 1, bytes.length)
      newBytes(0) = 1 // Set the most significant byte to 1
      Key(newBytes)
    } else {
      Key(incrementedBytes)
    }
  }

  // Function to represent the bytes as integers in the range [0, 255]
  def toStringAsIntArray: String = {
    val intArray = bytes.map(byteValue => byteValue & 0xFF)
    s"Key[${intArray.mkString(" ")}]"
  }
}

// Companion object for Key
object Key {
  val keySize = 10 // The size of the key in bytes

  // Deserialize a Key from a byte array
  def fromByteArray(byteArray: Array[Byte]): Key = Key(byteArray)
}
