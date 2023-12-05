package com.cs434.sortnet.core

import org.scalatest.funsuite.AnyFunSuite

import java.io.{ByteArrayInputStream, DataInputStream}

class ValueTest extends AnyFunSuite {

  test("Serialization and deserialization should work correctly") {
    val originalValue = Value(Array[Byte](1.toByte, 2.toByte, 3.toByte))
    val byteArray = originalValue.toByteArray
    val deserializedValue = Value.fromByteArray(byteArray)
    assert(originalValue.compare(deserializedValue) == 0)
  }

  test("String representation should be correct") {
    val value = Value(Array[Byte](1.toByte, 2.toByte, 3.toByte))
    assert(value.toString == "Value[1 2 3]")
  }

  // Add more test cases as needed for other methods in the Value class

}

