package com.cs434.sortnet.core

import java.io.{DataInputStream, DataOutputStream, IOException}

@SerialVersionUID(7361933779652783196L)
case class Record(key: Key, value: Value) extends Serializable {
  // Serialize the Record to a byte array
  def toByteArray: Array[Byte] = {
    try {
        val keyBytes = key.toByteArray
        val valueBytes = value.toByteArray
        val byteArray = new Array[Byte](keyBytes.length + valueBytes.length)

        // Copy the key bytes to the byteArray
        System.arraycopy(keyBytes, 0, byteArray, 0, keyBytes.length)

        // Copy the value bytes to the byteArray
        System.arraycopy(valueBytes, 0, byteArray, keyBytes.length, valueBytes.length)

        byteArray
    } catch {
        case e: IOException =>
        throw new RuntimeException("Error while serializing Record", e)
    }
  }
}

// Companion object for Record
object Record {
  val recordSize = 100 // 10 bytes for the key and 90 bytes for the value

  // Deserialize a Record from a DataInputStream
  def fromByteArray(dataInputStream: DataInputStream): Record = {
    val keyBytes = new Array[Byte](Key.keySize)
    dataInputStream.readFully(keyBytes)
    val valueBytes = new Array[Byte](Value.valueSize)
    dataInputStream.readFully(valueBytes)

    Record(Key(keyBytes), Value(valueBytes))
  }
}
