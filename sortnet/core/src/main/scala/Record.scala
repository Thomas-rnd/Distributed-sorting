package com.cs434.sortnet.core

import java.io.{DataInputStream, IOException, Serializable}

/**
 * Represents a record with a key and a value.
 *
 * @param key    The key associated with the record.
 * @param value  The value associated with the record.
 */
@SerialVersionUID(7361933779652783196L)
case class Record(key: Key, value: Value) extends Serializable {

  /**
   * Serializes the record to a byte array.
   *
   * @return The serialized byte array.
   */
  def toByteArray: Array[Byte] = {
    try {
      val keyBytes = key.toByteArray
      val valueBytes = value.toByteArray

      // Create a new byte array to hold both key and value
      val byteArray = new Array[Byte](keyBytes.length + valueBytes.length)

      System.arraycopy(keyBytes, 0, byteArray, 0, keyBytes.length)
      System.arraycopy(valueBytes, 0, byteArray, keyBytes.length, valueBytes.length)

      byteArray
    } catch {
      case e: IOException =>
        throw new RuntimeException("Error while serializing Record", e)
    }
  }
}

/**
 * Companion object for the Record class.
 */
object Record {
  // Size of a record in bytes (10 bytes for the key and 90 bytes for the value)
  val recordSize = 100

  /**
   * Deserializes a Record from a DataInputStream.
   *
   * @param dataInputStream The DataInputStream containing the serialized record.
   * @return A new Record instance.
   */
  def fromByteArray(dataInputStream: DataInputStream): Record = {
    // Read key bytes from the DataInputStream
    val keyBytes = new Array[Byte](Key.keySize)
    dataInputStream.readFully(keyBytes)

    // Read value bytes from the DataInputStream
    val valueBytes = new Array[Byte](Value.valueSize)
    dataInputStream.readFully(valueBytes)

    Record(Key(keyBytes), Value(valueBytes))
  }
}
