# Worker Documentation

This document provides comprehensive documentation for the `Worker` and `WorkerServices` classes in the Scala project located at `com.cs434.sortnet.worker`. These classes are responsible for handling various tasks related to worker nodes in a distributed sorting network.

## Classes Overview

- **Package:** `com.cs434.sortnet.worker`
- **Files:** `WorkerServices.scala`/`Worker.scala`

## `sendSamples(folderPath: String, input_data_type: String): List[Key]`

Sends sampled keys from the specified folder.

### Parameters:

- `folderPath`: Path to the folder containing data files.
- `input_data_type`: Input data type ("byte" or "ascii").

### Returns:

- List of sampled keys.

### Description:

This function reads data blocks from files in the specified folder, samples keys from them, and returns a list of sampled keys. It ensures that the folder exists, is a valid directory, and contains files to sample keys from.

## `sortFiles(folderPath: String, partitionPlan: PartitionPlan, input_data_type: String): List[Partition]`

Sorts files in the specified folder based on the given partition plan.

### Parameters:

- `folderPath`: Path to the folder containing data files.
- `partitionPlan`: The partition plan for sorting.
- `input_data_type`: Input data type ("byte" or "ascii").

### Returns:

- List of sorted partitions.

### Description:

This function reads data blocks from files, sorts the blocks, and partitions them based on the provided partition plan. It ensures that the folder exists, is a valid directory, and contains files for sorting.

## `handleSaveBlockRequest(numWorkers: Int, data_type: String): Unit`

Handles incoming SaveBlockRequest from other workers.

### Parameters:

- `numWorkers`: The total number of workers.
- `data_type`: Data type ("byte" or "ascii").

### Description:

This function establishes a server socket to listen for SaveBlockRequest from other workers. It handles multiple requests concurrently, spawns threads to process each request, and ensures successful execution.

## `saveBlocksFromWorker(clientSocket: Socket, saveBlockRequest: SaveBlockRequest, data_type: String): Unit`

Saves blocks received from another worker.

### Parameters:

- `clientSocket`: Socket for communication with the sending worker.
- `saveBlockRequest`: SaveBlockRequest object containing block information.
- `data_type`: Data type ("byte" or "ascii").

### Description:

This function saves blocks received from another worker to temporary files. It communicates with the sending worker through a socket and ensures error handling for robust operation.

## `sendSaveBlockRequest(partitionPlan: PartitionPlan, partitionsToSendList: List[Partition], input_data_type: String): Unit`

Sends SaveBlockRequest to other workers.

### Parameters:

- `partitionPlan`: The partition plan for distributing blocks.
- `partitionsToSendList`: List of partitions to send to other workers.
- `input_data_type`: Input data type ("byte" or "ascii").

### Description:

This function sends SaveBlockRequest to other workers based on the provided partition plan. It establishes connections to other workers, sends block data, and handles errors for reliable communication.

## `mergeFiles(folderPath: String, outputPath: String, input_data_type: String): Unit`

Merges files in the specified folder and writes the sorted output to a file.

### Parameters:

- `folderPath`: Path to the folder containing data files.
- `outputPath`: Path to the folder for writing the sorted output.
- `input_data_type`: Input data type ("byte" or "ascii").

### Description:

This function merges sorted partitions in the specified folder and writes the final sorted output to the specified output folder. It ensures the existence of the input folder, processes files iteratively, and updates the output accordingly.
