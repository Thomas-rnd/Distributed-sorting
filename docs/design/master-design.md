# Master Documentation

This document provides detailed documentation for the `Master` and `MasterServices` classes in the Scala project located at `com.cs434.sortnet.master`. The `Master` and `MasterServices` classes are responsible for handling registration requests from worker nodes and coordinating the distribution of sorting tasks among them.

## Class Overview

- **Package:** `com.cs434.sortnet.master`
- **File:** `MasterServices.scala`

## handleRegisterRequest

## sendRequests

## sendRequestThread

## `findPivotKeys(sortedSampledKeys: List[Key], numberOfWorkers: Int): List[Key]`

Finds pivot keys in a sorted list for partitioning.

### Parameters:

- `sortedSampledKeys`: A sorted list of sampled keys.
- `numberOfWorkers`: The number of workers/partitions.

### Returns:

- List of pivot keys.

### Description:

This function calculates pivot keys based on a sorted list of sampled keys and the specified number of workers. It ensures that there are enough keys for the specified number of workers and calculates the pivot index coefficient to determine the value of each pivot.

## `generateInterleavedPivotList(pivots: List[Key]): List[Key]`

Generates an interleaved list of pivot keys along with min and max keys.

### Parameters:

- `pivots`: List of pivot keys.

### Returns:

- Interleaved list of keys.

### Description:

This function generates an interleaved list of keys, including minKey, pivot, pivot + 1, and maxKey. It takes a list of pivot keys as input and ensures proper interleaving.

## `createKeyRangeByAggregatingKeys(list: List[Key]): List[KeyRange]`

Creates a list of KeyRange objects by aggregating keys.

### Parameters:

- `list`: List of keys to be aggregated.

### Returns:

- List of KeyRange objects.

### Description:

This function aggregates keys in pairs to create KeyRange objects. It ensures that the input list has an even number of elements for proper key range creation.

## `createKeyRangeFromSampledKeys(sampledKeys: List[Key], numberOfWorkers: Int): List[KeyRange]`

Performs sampling to create key ranges for partitioning.

### Parameters:

- `sampledKeys`: List containing a sample of keys.
- `numberOfWorkers`: The number of workers/partitions.

### Returns:

- List of KeyRange objects representing the partitioning.

### Description:

This function performs sampling on a list of keys, sorts them, finds pivot keys, generates interleaved sampling data, and creates KeyRange objects for partitioning. It ensures that the sampling data is not empty.

## `computePartitionPlan(sampledKeys: Map[String, List[Key]], numWorkers: Int): PartitionPlan`

Computes the partition plan based on sampled keys and the number of workers.

### Parameters:

- `sampledKeys`: Map containing sample keys for each worker.
- `numWorkers`: The number of workers/partitions.

### Returns:

- PartitionPlan object representing the partitioning plan.

### Description:

This function computes the partition plan by associating sampled keys with corresponding KeyRange objects. It returns a PartitionPlan object representing the partitioning plan.
