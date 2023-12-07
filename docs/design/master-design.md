# Master Documentation

This document provides detailed documentation for the `Master` and `MasterServices` classes in the Scala project located at `com.cs434.sortnet.master`. The `Master` and `MasterServices` classes are responsible for handling registration requests from worker nodes and coordinating the distribution of sorting tasks among them.

## Classes Overview

- **Package:** `com.cs434.sortnet.master`
- **Files:** `MasterServices.scala`/`Master.scala`

## `handleRegisterRequest(clientSocket: Socket, registerRequest: RegisterRequest, threadPool: ExecutorService, workerMetadataMap: Map[String, WorkerMetadata], numWorkers: Int): Unit`

The `handleRegisterRequest` function is responsible for processing registration requests from client sockets in a distributed system. It checks the current number of registered workers against a specified limit (numWorkers). If there is room for more workers, it registers the new worker and sends a positive acknowledgment (RegisterReply(true)); otherwise, it denies the registration and sends a negative acknowledgment (RegisterReply(false)).

### Parameters:

- `clientSocket`: Socket: The socket representing the client's connection.
- `registerRequest`: RegisterRequest: An object containing registration request details.
- `threadPool`: ExecutorService: An executor service for managing concurrent tasks.
- `workerMetadataMap`: Map[String, WorkerMetadata]: A map storing metadata information for registered workers.
- `numWorkers`: Int: The maximum allowed number of registered workers.

### Return:

Noting

## `sendRequests(workerMetadataMap: Map[String, WorkerMetadata], messageType: MessageType.Value,partitionPlan: Option[PartitionPlan] = None, sampleKeys: Option[Map[String, List[Key]]] = None, success: Option[Boolean] = None, reason: Option[String] = None): Unit`

The `sendRequests` function is responsible for concurrently sending various types of requests to multiple workers in a distributed system. It leverages multithreading to send different request types, such as sampling keys, saving partition plans, sorting, shuffling, merging, and termination, to worker nodes based on a specified message type.

### Parameters:

- `workerMetadataMap`: Map[String, WorkerMetadata]: A map containing metadata information for multiple workers.
- `messageType`: MessageType.Value: The type of message/request to be sent to all workers.
- `partitionPlan`: Option[PartitionPlan]: (Optional) The partition plan to be sent in case of a SavePartitionPlan request.
- `sampleKeys`: Option[Map[String, List[Key]]]: (Optional) The sampled keys to be sent in case of a SampleKey request.
- `success`: Option[Boolean]: (Optional) The success status for the Terminate request.
- `reason`: Option[String]: (Optional) The reason for termination in case of a Terminate request.

### Return:

Nothing

### Description:

The function concurrently sends the specified type of request to all workers in the provided workerMetadataMap. It utilizes multithreading to improve efficiency and handles each request type

## `sendRequestThread(workerMetadata: WorkerMetadata, messageType: MessageType.Value, partitionPlan: Option[PartitionPlan] = None, sampleKeys: Option[Map[String, List[Key]]] = None, success: Option[Boolean] = None, reason: Option[String] = None): Unit`

The `sendRequestThread` function is responsible for sending various types of requests to a worker in a distributed system. It facilitates communication between the master node and worker nodes, supporting operations such as sampling keys, saving partition plans, sorting, shuffling, merging, and termination.

### Parameters:

- `workerMetadata`: WorkerMetadata: Metadata information for the target worker.
- `messageType`: MessageType.Value: The type of message/request to be sent.
- `partitionPlan`: Option[PartitionPlan]: (Optional) The partition plan to be sent in case of a SavePartitionPlan request.
- `sampleKeys`: Option[Map[String, List[Key]]]: (Optional) The sampled keys to be sent in case of a SampleKey request.
- `success`: Option[Boolean]: (Optional) The success status for the Terminate request.
- `reason`: Option[String]: (Optional) The reason for termination in case of a Terminate request.

### Returns:

Nothing

### Description:

The function establishes a connection with the specified worker through a socket and sends different types of requests based on the provided messageType. It handles various request types

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
