package com.cs434.sortnet.core

import java.io.Serializable

// Define the KeyRange case class
case class KeyRange(startKey: Key, endKey: Option[Key]) extends Serializable {
  override def toString: String = {
    val startKeyString = startKey.toString
    val endKeyString = endKey.map(_.toString).getOrElse("None")
    s"KeyRange($startKeyString, $endKeyString)"
  }
}
