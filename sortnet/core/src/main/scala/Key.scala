package com.cs434.sortnet.core

import java.io.{DataInputStream, DataOutputStream, IOException}
import java.util.Arrays

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
    val byteArrayAsString = bytes.map(byte => f"$byte%02X").mkString(", ")
    s"Key([$byteArrayAsString])"
  }
}

// Companion object for Key
object Key {
  val keySize = 10 // The size of the key in bytes

  // Deserialize a Key from a byte array
  def fromByteArray(byteArray: Array[Byte]): Key = Key(byteArray)
}
