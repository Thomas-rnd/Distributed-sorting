package com.cs434.sortnet.core

/**
 * Represents a range of keys in the sorting network.
 *
 * @param startKey The starting key of the range.
 * @param endKey   The ending key of the range.
 */
@SerialVersionUID(7161933779652783196L)
case class KeyRange(startKey: Key, endKey: Key) extends Serializable {

  /**
   * Returns a string representation of the key range.
   *
   * @return The string representation.
   */
  override def toString: String = {
    val startKeyString = startKey.toString
    val endKeyString = endKey.toString
    s"KeyRange($startKeyString, $endKeyString)"
  }

}
