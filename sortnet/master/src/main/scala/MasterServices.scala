class MasterServices {
    // Calculate key ranges from sampled keys received from workers
    def calculateKeyRanges(sampledKeys: List[Key], numWorkers: Int): List[KeyRange] = {
        val sortedKeys = sampledKeys.sorted
        val numPivots = numWorkers - 1
        // Calculating the indices of pivot points
        val pivotIndices = (1 to numPivots).map { i =>
        (i * sortedKeys.size) / numWorkers
        }
        // Creating KeyRange tuples based on pivot points
        val keyRanges = pivotIndices.zip(pivotIndices.tail :+ sortedKeys.size).map {
        case (startIdx, endIdx) =>
            val startKey = sortedKeys(startIdx)
            val endKey = if (endIdx < sortedKeys.size) sortedKeys(endIdx - 1) else -1
            (startKey, endKey)
        }.toList

        keyRanges
    }
  
}
