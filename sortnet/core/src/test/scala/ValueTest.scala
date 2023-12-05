package com.cs434.sortnet.core

import org.scalatest.funsuite.AnyFunSuite

import java.io.{ByteArrayInputStream, DataInputStream}

class ValueTest extends AnyFunSuite {

  test("Serialization and deserialization should work correctly") {
    val originalValue = Value("  00000000000000000000000000000000  0000222200002222000022220000222200002222000000001111\r\n".getBytes("UTF-8"))
    val byteArray = originalValue.toByteArray
    val deserializedValue = Value.fromByteArray(byteArray)
    assert(originalValue.compare(deserializedValue) == 0)
  }
}

