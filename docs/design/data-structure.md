# Data Structures Documentation

This document provides an overview of the data structures used in our Scala project.

## Key

- **Description:** The `Key` data structure represents a 10-byte key used for sorting and comparison. It is also serializable, allowing for easy storage and transmission of keys.
- **Type:** `Array[Byte]`
- **Usage:** Original data for the key.
- **Features:**
  - **Comparison:** `Key` instances are comparable, thanks to the `Ordered` trait. They can be used for sorting and comparing keys and Record in extension.
  - **Serialization:** `Key` is serializable, allowing it to be easily converted to and from a byte array for storage and transmission.
- **Methods:**
  - `toByteArray: Array[Byte]`: Serializes the key to a byte array.
  - `fromByteArray(bytes: Array[Byte]): Key`: Deserializes a byte array to a key.

![key drawio](https://github.com/AlexDevauchelle/434project/assets/70631774/94935a69-ea54-4a6a-b2c2-719e86c9d95a)

## Value

- **Description:** The `Value` data structure represents a 90-byte value associated with a `Key` in a `Record`.
- **Type:** `Array[Byte]`
- **Usage:** Data associated with a key.
- **Features:**
  - **Serialization:** `Value` is serializable, allowing it to be easily converted to and from a byte array for storage and transmission.
- **Methods:**
  - `toByteArray: Array[Byte]`: Serializes the value to a byte array.
  - `fromByteArray(bytes: Array[Byte]): Value`: Deserializes a byte array to a value.

![value drawio](https://github.com/AlexDevauchelle/434project/assets/70631774/4523b891-f9d0-4c13-a476-68cd04c290ed)

## Record

- **Description:** The `Record` data structure represents a combination of a 10-byte `Key` and a 90-byte `Value`.
- **Key Type:** `Key`
- **Value Type:** `Value`
- **Features:**
  - **Serialization:** `Record` is serializable, allowing it to be easily converted to and from a byte array for storage and transmission.
- **Methods:**
  - `toByteArray: Array[Byte]`: Serializes the record to a byte array.
  - `fromByteArray(bytes: Array[Byte]): Record`: Deserializes a byte array to a record.

![Record drawio](https://github.com/AlexDevauchelle/434project/assets/70631774/cd2afd59-efe6-455d-bd45-38f5321b790e)

## Block

- **Description:** The `Block` data structure represents a list of `Record` objects and is used for sorting, serialization, and deserialization.
- **Record Type:** `Record`
- **Features:**
  - **Serialization:** `Block` is serializable, allowing it to be easily converted to and from a byte array for storage 
  - **Sorting:** The `Block` class offers a sorting method that arranges records based on their keys, facilitating data organization.
  - **Partitioning:** With a `PartitionPlan`, you can partition a `Block`, enabling the distribution of records to various workers. Optionally, you can filter records by a specified key range during partitioning.
  - **Sampling:** The `Block` class provides a feature for sampling a defined number of keys from its records, creating a subset of keys for analytical or testing purposes.
- **Methods:**
  - `sorted: Block`: Sorts the records within the block based on the `key` attribute.
  - `toByteArray: Array[Byte]`: Serializes the block into a byte array.
  - `fromByteArray(bytes: Array[Byte]): Block`: Deserializes a byte array into a `Block`.
  - `readFromFile(filename: String): Block`: Reads a `Block` from a file with the given `filename`.
  - `writeToFile(block: Block, filename: String): Unit`: Writes the contents of a `Block` to a file with the specified `filename`.
  - `partition(plan: PartitionPlan, block: Block): List[Partition]`: Partitions the `Block` based on the provided `PartitionPlan` and returns a list of `Partition` objects.
  - `sampleKeys(block: Block, numSamples: Int): List[Key]`: Samples keys from the `Block` to generate a list of sampled keys.

![Block drawio](https://github.com/AlexDevauchelle/434project/assets/70631774/6bc12f7f-3614-4f38-9d37-be572b3a58fe)

## KeyRange

- **Description:** The `KeyRange` data structure represents a range of keys defined by a starting and ending key.
- **Type:** `Tuple2[Key, Key]`
- **Usage:** Used for specifying key ranges in the system.

![KeyRange drawio](https://github.com/AlexDevauchelle/434project/assets/70631774/554deb05-7c60-4c48-9d16-9247a760ea3d)

## Partition

- **Description:** The `Partition` data structure represents a pairing of two `String`.
- **String Type:** `ip`
- **String Type:** `pathToBlockFile`
- **Usage:** Used for partitioning data among workers.

![partition drawio](https://github.com/AlexDevauchelle/434project/assets/70631774/75ce060a-713e-45ce-8654-91734326cacd)

## PartitionPlan

- **Description:** The `PartitionPlan` data structure represents a collection of worker ip along with their associated key ranges.
- **Type:** `List[(String, KeyRange)]`
- **Usage:** Used for planning data partitioning among workers.
- **Features:**
  - **Serialization:** `PartitionPlan` is serializable, allowing it to be easily converted to and from a byte array for storage 

![partitionPlan drawio](https://github.com/AlexDevauchelle/434project/assets/70631774/bd26c113-acb4-4b5b-b398-fba1a83c9423)

## WorkerMetadata

- **Description:** The `WorkerMetadata` data structure represents information about a worker, including its key range, IP address, port, and socket.
- **Key Range Type:** `Option[KeyRange]`
- **IP Type:** `String`
- **Port Type:** `Int`

![WorkerMD drawio](https://github.com/AlexDevauchelle/434project/assets/70631774/2264c206-ff86-45bd-bcd9-72ae94ff6c9f)

## MasterMetadata

- **Description:** The `MasterMetadata` data structure represents metadata for the master, including the host (IP address) and port.
- **Host Type:** `String`
- **Port Type:** `Int`

![MasterMD drawio](https://github.com/AlexDevauchelle/434project/assets/70631774/33b2b6cf-c5d4-445e-9c50-caa5c7a202c3)

---

This documentation provides an overview of the key data structures used in our project, including their descriptions, types, and usage. It can serve as a reference for understanding the data structures in the system.
