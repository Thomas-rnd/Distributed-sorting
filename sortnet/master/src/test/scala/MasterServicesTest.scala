package com.cs434.sortnet.master

import org.scalatest.funsuite.AnyFunSuite
import scala.collection.mutable.Map
import com.cs434.sortnet.core._

class MasterServicesTest extends AnyFunSuite {

  test("findPivotKeys should return correct pivots") {
    val sortedSampledKeys = List(
      Key(Array.fill(Key.keySize)(1.toByte)),
      Key(Array.fill(Key.keySize)(2.toByte)),
      Key(Array.fill(Key.keySize)(3.toByte)),
      Key(Array.fill(Key.keySize)(4.toByte)),
      Key(Array.fill(Key.keySize)(5.toByte)),
      Key(Array.fill(Key.keySize)(6.toByte)),
      Key(Array.fill(Key.keySize)(7.toByte)),
      Key(Array.fill(Key.keySize)(8.toByte)),
      Key(Array.fill(Key.keySize)(9.toByte))
    )

    val numberOfWorkers = 3
    val result = MasterServices.findPivotKeys(sortedSampledKeys, numberOfWorkers)

    assert(result.size == numberOfWorkers-1)
    assert(result(0).compare(Key(Array.fill(Key.keySize)(4.toByte))) == 0)
    assert(result(1).compare(Key(Array.fill(Key.keySize)(7.toByte))) == 0)
  }

  test("generateInterleavedPivotList should return correct interleaved list") {
    val pivots = List(
      Key(Array.fill(Key.keySize)(4.toByte)),
      Key(Array.fill(Key.keySize)(7.toByte))
    )

    val result = MasterServices.generateInterleavedPivotList(pivots)

    assert(result.size == 6)
    assert(result(0).compare(Key(Array.fill(Key.keySize)(0.toByte))) == 0)
    assert(result(1).compare(Key(Array.fill(Key.keySize)(4.toByte))) == 0)
    assert(result(2).compare(Key(Array.fill(Key.keySize)(4.toByte)).incrementByOne) == 0)
    assert(result(3).compare(Key(Array.fill(Key.keySize)(7.toByte))) == 0)
    assert(result(4).compare(Key(Array.fill(Key.keySize)(7.toByte)).incrementByOne) == 0)
    assert(result(5).compare(Key(Array.fill(Key.keySize)(-127.toByte))) == 0)
  }

  test("createKeyRangeByAggregatingKeys should return correct KeyRange list") {
    val keys = List(
      Key(Array.fill(Key.keySize)(0.toByte)),
      Key(Array.fill(Key.keySize)(4.toByte)),
      Key(Array.fill(Key.keySize)(7.toByte)),
      Key(Array.fill(Key.keySize)(-127.toByte)),
    )

    val result = MasterServices.createKeyRangeByAggregatingKeys(keys)

    assert(result.size == 2)
    assert(result(0).toString == KeyRange(Key(Array.fill(Key.keySize)(0.toByte)), Key(Array.fill(Key.keySize)(4.toByte))).toString)
    assert(result(1).toString == KeyRange(Key(Array.fill(Key.keySize)(7.toByte)), Key(Array.fill(Key.keySize)(-127.toByte))).toString)
  }

  test("createKeyRangeFromSampledKeys should return correct KeyRange list") {
    val sampledKeys = List(
      Key(Array.fill(Key.keySize)(1.toByte)),
      Key(Array.fill(Key.keySize)(8.toByte)),
      Key(Array.fill(Key.keySize)(2.toByte)),
      Key(Array.fill(Key.keySize)(6.toByte)),
      Key(Array.fill(Key.keySize)(7.toByte)),
      Key(Array.fill(Key.keySize)(3.toByte)),
      Key(Array.fill(Key.keySize)(4.toByte)),
      Key(Array.fill(Key.keySize)(5.toByte)),
      Key(Array.fill(Key.keySize)(9.toByte))
    )

    val numberOfWorkers = 3
    val result = MasterServices.createKeyRangeFromSampledKeys(sampledKeys, numberOfWorkers)

    assert(result.size == 3)
    assert(result(0).toString == KeyRange(Key(Array.fill(Key.keySize)(0.toByte)), Key(Array.fill(Key.keySize)(4.toByte))).toString)
    assert(result(1).toString == KeyRange(Key(Array.fill(Key.keySize)(4.toByte)).incrementByOne, Key(Array.fill(Key.keySize)(7.toByte))).toString)
    assert(result(2).toString == KeyRange(Key(Array.fill(Key.keySize)(7.toByte)).incrementByOne, Key(Array.fill(Key.keySize)(-127.toByte))).toString)
  }

  test("computePartitionPlan should return correct PartitionPlan") {
    val sampledKeys = Map(
      "worker1" -> List(
        Key(Array.fill(Key.keySize)(1.toByte)),
        Key(Array.fill(Key.keySize)(8.toByte)),
        Key(Array.fill(Key.keySize)(2.toByte))
      ),
      "worker2" -> List(
        Key(Array.fill(Key.keySize)(6.toByte)),
        Key(Array.fill(Key.keySize)(7.toByte)),
        Key(Array.fill(Key.keySize)(3.toByte))
      ),
      "worker3" -> List(
        Key(Array.fill(Key.keySize)(4.toByte)),
        Key(Array.fill(Key.keySize)(5.toByte)),
        Key(Array.fill(Key.keySize)(9.toByte))
      )
    )

    val numberOfWorkers = 3
    val result = MasterServices.computePartitionPlan(sampledKeys, numberOfWorkers)

    assert(result.partitions.nonEmpty)
    assert(result.toString == PartitionPlan(Array(
      ("worker1", KeyRange(Key(Array.fill(Key.keySize)(0.toByte)), Key(Array.fill(Key.keySize)(4.toByte)))),
      ("worker2", KeyRange(Key(Array.fill(Key.keySize)(4.toByte)).incrementByOne, Key(Array.fill(Key.keySize)(7.toByte)))),
      ("worker3", KeyRange(Key(Array.fill(Key.keySize)(7.toByte)).incrementByOne, Key(Array.fill(Key.keySize)(-127.toByte))))
    )).toString)
  }
}
