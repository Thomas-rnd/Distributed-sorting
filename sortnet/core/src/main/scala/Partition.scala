package com.cs434.sortnet.core

/**
 * Represents a partition of data in the sorting network.
 *
 * @param ip               The IP address associated with the partition.
 * @param pathToBlockFile  The file path to the block file containing the partitioned data.
 */
case class Partition(ip: String, pathToBlockFile: String)
