package com.cs434.sortnet.core

import java.io.Serializable

@SerialVersionUID(7161933779652783196L)
case class KeyRange(startKey: Key, endKey: Key) extends Serializable {
  override def toString: String = {
    val startKeyString = startKey.toString
    val endKeyString = endKey.toString
    s"KeyRange($startKeyString, $endKeyString)"
  }

  def toStringAsIntArray: String = {
    s"KeyRange[${startKey.toStringAsIntArray} - ${endKey.toStringAsIntArray}]"
  }
}
