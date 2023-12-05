package com.cs434.sortnet.core

import java.io.{ByteArrayInputStream, DataInputStream, ByteArrayOutputStream, DataOutputStream}

import org.scalatest.funsuite.AnyFunSuite

class RecordTest extends AnyFunSuite {

  test("Serialization and deserialization should work correctly") {
    // Create a sample Key and Value with the required size
    val keyBytes = Array[Byte](1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    val valueBytes = Array.fill(Value.valueSize)(0.toByte)

    val key = Key(keyBytes)
    val value = Value(valueBytes)

    // Create an original Record
    val originalRecord = Record(key, value)

    // Test serialization
    val byteArrayOutputStream = new ByteArrayOutputStream()
    val dataOutputStream = new DataOutputStream(byteArrayOutputStream)

    dataOutputStream.write(originalRecord.toByteArray)
    dataOutputStream.close()
    val byteArray = byteArrayOutputStream.toByteArray

    // Ensure the serialized byte array has the expected size
    assert(byteArray.length == Key.keySize + Value.valueSize)

    // Test deserialization
    val byteArrayInputStream = new ByteArrayInputStream(byteArray)
    val dataInputStream = new DataInputStream(byteArrayInputStream)

    val deserializedRecord = Record.fromByteArray(dataInputStream)

    // Assertions
    assert(originalRecord.key.compare(deserializedRecord.key)==0)
    assert(originalRecord.value.compare(deserializedRecord.value)==0)
  }
}
