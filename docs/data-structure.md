# Data Structures Documentation

This document provides an overview of the data structures used in our Scala project.

## Key

- **Description:** The `Key` data structure represents a unique key used for sorting and comparison.
- **Type:** `Array[Byte]`
- **Usage:** Original data for the key.

## Value

- **Description:** The `Value` data structure represents a 90-byte value associated with a `Key`.
- **Type:** `Array[Byte`
- **Usage:** Data associated with a key.

## Record

- **Description:** The `Record` data structure represents a combination of a 10-byte `Key` and a 90-byte `Value`.
- **Key Type:** `Key`
- **Value Type:** `Value`
- **Methods:**
  - `toByteArray: Array[Byte]`: Serializes the record to a byte array.
  - `fromByteArray(bytes: Array[Byte]): Record`: Deserializes a byte array to a record.

## Block

- **Description:** The `Block` data structure contains a list of `Record`s and is used for sorting, serialization, and deserialization.
- **Record Type:** `Record`
- **Methods:**
  - `sorted: Block`: Sorts the records based on the Key.
  - `toByteArray: Array[Byte]`: Serializes the block to a byte array.
  - `fromByteArray(bytes: Array[Byte]): Block`: Deserializes a byte array to a block.
  - `readFromFile(filename: String): Block`: Reads a block from a file.
  - `writeToFile(block: Block, filename: String): Unit`: Writes a block to a file.
  - `partition(partitionPlan: PartitionPlan): List[Partition]`: Partitions the block based on a given partition plan.

## KeyRange

- **Description:** The `KeyRange` data structure represents a range of keys defined by a starting and ending key.
- **Type:** `Tuple2[Key, Key]`
- **Usage:** Used for specifying key ranges in the system.

## Partition

- **Description:** The `Partition` data structure represents a pairing of `WorkerMetadata` and a `Block`.
- **WorkerMetadata Type:** `WorkerMetadata`
- **Block Type:** `Block`
- **Usage:** Used for partitioning data among workers.

## PartitionPlan

- **Description:** The `PartitionPlan` data structure represents a collection of worker metadata along with their associated key ranges.
- **Type:** `List[(WorkerMetadata, Option[KeyRange])]`
- **Usage:** Used for planning data partitioning among workers.

## WorkerMetadata

- **Description:** The `WorkerMetadata` data structure represents information about a worker, including its key range, IP address, port, and socket.
- **Key Range Type:** `Option[KeyRange]`
- **IP Type:** `String`
- **Port Type:** `Int`

## MasterMetadata

- **Description:** The `MasterMetadata` data structure represents metadata for the master, including the host (IP address) and port.
- **Host Type:** `String`
- **Port Type:** `Int`

---

This documentation provides an overview of the key data structures used in our project, including their descriptions, types, and usage. It can serve as a reference for understanding the data structures in the system.
