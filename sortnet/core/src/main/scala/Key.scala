package com.cs434.sortnet.core
import java.io.IOException

/**
 * Represents a key used in sorting and partitioning.
 *
 * @param bytes The byte array representation of the key.
 */
@SerialVersionUID(3403353340572833574L)
case class Key(bytes: Array[Byte]) extends Ordered[Key] with Serializable {

  require(bytes.length == Key.keySize, s"Key must have exactly ${Key.keySize} bytes")


  def toUnsignedByte(byteValue: Byte): Int = {
    byteValue.toInt & 0xFF
  }
  
  /**
   * Compares this key with another key.
   *
   * @param that The other key to compare with.
   * @return A negative integer, zero, or a positive integer as this key is less than, equal to,
   *         or greater than the specified key.
   */
  def compare(that: Key): Int = {
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
   * Serializes the Key to a byte array.
   *
   * @return The serialized byte array.
   */
  def toByteArray: Array[Byte] = {
    try {
      val byteArray = new Array[Byte](bytes.length)
      Array.copy(bytes, 0, byteArray, 0, bytes.length)
      assert(java.util.Arrays.equals(byteArray, bytes), "Array copy failed")
      byteArray
    } catch {
      case e: IOException =>
        throw new RuntimeException("Error while serializing Key", e)
    }
  }

  /**
   * Returns a string representation of the key.
   *
   * @return The string representation.
   */
  override def toString: String = {
    val intArray = bytes.map(byteValue => byteValue & 0xFF)
    s"Key[${intArray.mkString(" ")}]"
  }

  /**
   * Increments the key value by 1.
   *
   * @return The new Key after incrementing.
   */
  def incrementByOne: Key = {
    val incrementedBytes = new Array[Byte](bytes.length)
    var carry = 1

    // Start from the end of the byte array (LSB) and work towards the beginning (MSB)
    for (i <- bytes.length - 1 to 0 by -1) {
      val sum = (bytes(i) & 0xFF) + carry
      incrementedBytes(i) = (sum & 0xFF).toByte
      carry = sum >> 8
    }

    // If there's still a carry after the loop, it means we've overflowed
    if (carry != 0) {
      // Handle overflow by creating a new larger byte array and copying the incrementedBytes into it
      val newBytes = new Array[Byte](bytes.length + 1)
      Array.copy(incrementedBytes, 0, newBytes, 1, bytes.length)
      newBytes(0) = 1 // Set the most significant byte to 1
      assert(newBytes.sameElements(incrementedBytes ++ Array[Byte](1)), "Increment overflow failed")
      Key(newBytes)
    } else {
      Key(incrementedBytes)
    }
  }
  
}

// Companion object for Key
object Key {
  val keySize = 10 // The size of the key in bytes

  /**
   * Deserializes a Key from a byte array.
   *
   * @param byteArray The byte array to deserialize.
   * @return A new Key instance.
   */
  def fromByteArray(byteArray: Array[Byte]): Key = Key(byteArray)
}
