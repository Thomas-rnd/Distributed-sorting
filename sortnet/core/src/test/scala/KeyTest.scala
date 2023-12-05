package com.cs434.sortnet.core

import org.scalatest.funsuite.AnyFunSuite

class KeyTest extends AnyFunSuite{

  test("Keys should be ordered correctly") {
    val key1 = Key(Array[Byte](0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 1.toByte, 2.toByte, 3.toByte))
    val key2 = Key(Array[Byte](0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 1.toByte, 2.toByte, 4.toByte))
    assert(key1 < key2)
  }

  test("Serialization and deserialization should work correctly") {
    val originalKey = Key(Array[Byte](0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 1.toByte, 2.toByte, 3.toByte))
    val byteArray = originalKey.toByteArray
    val deserializedKey = Key.fromByteArray(byteArray)
    assert(originalKey.compare(deserializedKey) == 0)
  }

  test("Incrementing keys should produce the correct result") {
    val key1 = Key(Array[Byte](0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 1.toByte, 2.toByte, 3.toByte))
    val key2 = key1.incrementByOne
    assert(key2.compare(Key(Array[Byte](0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 1.toByte, 2.toByte, 4.toByte))) == 0)
  }

  test("String representation as int array should be correct") {
    val key = Key(Array[Byte](0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 1.toByte, 2.toByte, 3.toByte))
    assert(key.toString == "Key[0 0 0 0 0 0 0 1 2 3]")
  }

  // Add more test cases as needed for other methods in the Key class

}
