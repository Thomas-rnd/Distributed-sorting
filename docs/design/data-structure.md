# Data Structures Documentation 2.0

This document provides an overview of the data structures  used in our Scala project.

## Key

- **Description:** The `Key` data structure represents a 10-byte key used for sorting and comparison. It is also serializable, allowing for easy storage and transmission of keys.
- **Type:** `Array[Byte]`
- **Usage:** Original data for the key.
- **Features:**
  - **Comparison:** `Key` instances are comparable, thanks to the `Ordered` trait. They can be used for sorting and comparing keys and Record in extension.
  - **Serialization:** `Key` is serializable, allowing it to be easily converted to and from a byte array for storage and transmission.
- **Methods:**
  - `toByteArray: Array[Byte]`: Serializes the key to a byte array.
  - `toString: String`: Provides a string representation of the key. For better readability, it converts the byte array to a UTF-8 string.
  - `toStringAsIntArray: String`: Represents the key bytes as integers.
  - `incrementByOne: Key`: Increments the key value by 1. Handles overflow by creating a new larger byte array if needed.
  - `fromByteArray(bytes: Array[Byte]): Key`: Deserializes a byte array to a key.

![Key drawio](https://github.com/AlexDevauchelle/434project/assets/70631774/bc8fa13c-3ea1-4e86-bbce-acaaa4f75828)

## Value

- **Description:** The `Value` data structure represents a 90-byte value associated with a `Key` in a `Record`.
- **Type:** `Array[Byte]`
- **Usage:** Data associated with a key.
- **Features:**
  - **Serialization:** `Value` is serializable, allowing it to be easily converted to and from a byte array for storage and transmission.
- **Methods:**
  - `toByteArray: Array[Byte]`: Serializes the value to a byte array.
  - `toString: String`: Provides a string representation of the value. For better readability, it converts the byte array to a UTF-8 string.
  - `fromByteArray(bytes: Array[Byte]): Value`: Deserializes a byte array to a value.

![Value drawio](https://github.com/AlexDevauchelle/434project/assets/70631774/eca4ef4c-1f04-4dd1-93ce-2d4d4b5ae127)

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

- **Description:** The `Block` data structure represents a list of `Record` objects and is used for sampling, sorting, partitioning, serialization, and deserialization.
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
  - `readFromBinaryFile(filePath: String): Block`: Reads a `Block` from a binary file with the given `filePath`.
  - `readFromASCIIFile(filePath: String): Block`: Reads a `Block` from a ASCII file with the given `filePath`.
  - `writeToBinaryFile(block: Block, filePath: String): Unit`: Writes the contents of a `Block` to a binary file with the specified `filePath`.
  - `writeToASCIIFile(block: Block, filePath: String): Unit`: Writes the contents of a `Block` to a ASCII file with the specified `filePath`.
 - `partition(block: Block, plan: PartitionPlan, nameFile: String): List[Partition]`: Partitions the `Block` based on the provided `PartitionPlan`, creates ASCII files of resulting blocks, and returns a list of `Partition` objects.

  - `sampleKeys(block: Block, maxSizeBytes: Int): List[Key]`: Samples keys from the `Block` to generate a list of sampled keys.

![Block drawio](https://github.com/AlexDevauchelle/434project/assets/70631774/839ee778-ae64-45a9-b48a-53813f97f39b)

## KeyRange

- **Description:** The `KeyRange` data structure represents a range of keys defined by a starting and ending key.
- **Type:** `Tuple2[Key, Key]`
- **Usage:** Used for specifying key ranges in the system.
- **Methods:**
  - `toString: String`: Provides a string representation of the KeyRange. For better readability, it converts the byte array of the keys to a UTF-8 string.
  - `toStringAsIntArray: String`: Represents the keys bytes as integers.

![KeyRange drawio](https://github.com/AlexDevauchelle/434project/assets/70631774/2f9cca08-0057-41b9-9286-50588cf84f96)

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
- **Methods:**
  - `toString: String`: Provides a string representation of the PartitionPlan. For better readability, it converts the byte array of the keys to a UTF-8 string.

![ParitionPlan drawio](https://github.com/AlexDevauchelle/434project/assets/70631774/1e548918-eb6c-4671-aaeb-c1c4f8c1c540)

## WorkerMetadata

- **Description:** The `WorkerMetadata` data structure represents information about a worker, including its key range, IP address, port, and socket.
- **Key Range Type:** `Option[KeyRange]`
- **IP Type:** `String`
- **Port Type:** `Int`
- **Socker Type:** `Socket`

![WorkerMD drawio](https://github.com/AlexDevauchelle/434project/assets/70631774/2264c206-ff86-45bd-bcd9-72ae94ff6c9f)

## MasterMetadata

- **Description:** The `MasterMetadata` data structure represents metadata for the master, including the host (IP address), port and socket.
- **IP Type:** `String`
- **Port Type:** `Int`
- **Socker Type:** `Socket`

![MasterMetadata drawio](https://github.com/AlexDevauchelle/434project/assets/70631774/f2b9c3fb-2206-449f-a1b0-b85e8bd9cd6d)

---

This documentation provides an overview of the key data structures used in our project, including their descriptions, types, and usage. It can serve as a reference for understanding the data structures in the system.
