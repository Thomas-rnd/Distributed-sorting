# Network Message Structures Documentation

This document provides an overview of the network message structures used in our Scala project.

## RegisterRequest

- **Description:** A message sent from a Worker to the Master to request registration.
- **Usage:**
  - **Source:** Worker
  - **Destination:** Master
  - **Utility:** Initiates the registration process.
- **Parameter:**
  - `None`: This message has no additional parameters.

![RegReq drawio](https://github.com/AlexDevauchelle/434project/assets/70631774/4427d7bc-181e-4de4-946a-ca80cbfc442b)

## RegisterReply

- **Description:** A message sent from the Master to a Worker in response to a registration request.
- **Usage:**
  - **Source:** Master
  - **Destination:** Worker
  - **Utility:** Confirms or denies the success of registration.
- **Parameter:**
  - `success: Boolean`: Indicates whether the registration request was successful.

![RegReply drawio](https://github.com/AlexDevauchelle/434project/assets/70631774/5331b2f2-5c39-43f6-b18d-c2afd9c51515)

## SampleKeyRequest

- **Description:** A message sent from the Master to a Worker to request key sampling.
- **Usage:**
  - **Source:** Master
  - **Destination:** Worker
  - **Utility:** Requests the Worker to sample keys.
- **Parameter:**
  - `None`: This message has no additional parameters.

![SamplReq drawio](https://github.com/AlexDevauchelle/434project/assets/70631774/1406830c-7c7d-455f-b5a4-141b810e1751)

## SampleKeyReply

- **Description:** A message sent from a Worker to the Master in response to a key sampling request.
- **Usage:**
  - **Source:** Worker
  - **Destination:** Master
  - **Utility:** Provides sampled keys to the Master.
- **Parameter:**
  - `success: Boolean`: Indicates whether the key sampling request was successful.
  - `sampledKeys: List[Key]`: The list of sampled keys.

![SampleReply drawio](https://github.com/AlexDevauchelle/434project/assets/70631774/4bbb031f-59db-459f-ba62-295cad13249e)

## SortRequest

- **Description:** A message sent from the Master to a Worker to request sorting.
- **Usage:**
  - **Source:** Master
  - **Destination:** Worker
  - **Utility:** Requests the Worker to perform sorting.
- **Parameter:**
  - `None`: This message has no additional parameters.

![SortReq drawio](https://github.com/AlexDevauchelle/434project/assets/70631774/8eda2a6d-6373-4c0e-ab56-91c03497dfc6)

## SortReply

- **Description:** A message sent from a Worker to the Master in response to a sorting request.
- **Usage:**
  - **Source:** Worker
  - **Destination:** Master
  - **Utility:** Confirms or denies the success of the sorting operation.
- **Parameter:**
  - `success: Boolean`: Indicates whether the sorting request was successful.

![SortReply drawio](https://github.com/AlexDevauchelle/434project/assets/70631774/e67c52d3-ab8b-4cde-b48b-7910aa88281c)

## PartitionRequest

- **Description:** A message sent from the Master to a Worker to request partitioning.
- **Usage:**
  - **Source:** Master
  - **Destination:** Worker
  - **Utility:** Requests the Worker to perform partitioning.
- **Parameter:**
  - `None`: This message has no additional parameters.

![PartiReq drawio](https://github.com/AlexDevauchelle/434project/assets/70631774/9dabd0dd-372a-4ba4-96f7-8dd73542aa32)

## PartitionReply

- **Description:** A message sent from a Worker to the Master in response to a partitioning request.
- **Usage:**
  - **Source:** Worker
  - **Destination:** Master
  - **Utility:** Confirms or denies the success of the partitioning operation.
- **Parameter:**
  - `success: Boolean`: Indicates whether the partitioning request was successful.

![PartiReply drawio](https://github.com/AlexDevauchelle/434project/assets/70631774/36176329-37cf-4c7d-be7d-8c11f44beb00)

## ShuffleRequest

- **Description:** A message sent from the Master to a Worker to request shuffling.
- **Usage:**
  - **Source:** Master
  - **Destination:** Worker
  - **Utility:** Requests the Worker to perform shuffling.
- **Parameter:**
  - `None`: This message has no additional parameters.

![ShuffleReq drawio](https://github.com/AlexDevauchelle/434project/assets/70631774/1a45461a-ba9c-461c-b13b-405dcbfcf41f)

## ShuffleReply

- **Description:** A message sent from a Worker to the Master in response to a shuffling request.
- **Usage:**
  - **Source:** Worker
  - **Destination:** Master
  - **Utility:** Confirms or denies the success of the shuffling operation.
- **Parameter:**
  - `success: Boolean`: Indicates whether the shuffling request was successful.

![huffleReply drawio](https://github.com/AlexDevauchelle/434project/assets/70631774/7c98d1f6-af32-4683-9981-44324df3fa36)

## MergeRequest

- **Description:** A message sent from the Master to a Worker to request merging.
- **Usage:**
  - **Source:** Master
  - **Destination:** Worker
  - **Utility:** Requests the Worker to perform merging.
- **Parameter:**
  - `None`: This message has no additional parameters.

![MergeReq drawio](https://github.com/AlexDevauchelle/434project/assets/70631774/024914bd-f1d4-4dd8-9216-713a33e2b563)

## MergeReply

- **Description:** A message sent from a Worker to the Master in response to a merging request.
- **Usage:**
  - **Source:** Worker
  - **Destination:** Master
  - **Utility:** Confirms or denies the success of the merging operation.
- **Parameter:**
  - `success: Boolean`: Indicates whether the merging request was successful.

![MergeReply drawio](https://github.com/AlexDevauchelle/434project/assets/70631774/6959fabd-d5bc-44a9-886b-0f7be576cfca)

## TerminateRequest

- **Description:** A message sent from the Master to a Worker to request termination.
- **Usage:**
  - **Source:** Master
  - **Destination:** Worker
  - **Utility:** Requests the Worker to terminate its operation.
- **Parameter:**
  - `None`: This message has no additional parameters.

![terminateReq drawio](https://github.com/AlexDevauchelle/434project/assets/70631774/bba383b3-e28d-4ffa-84c4-88b7385e9c85)

## TerminateReply

- **Description:** A message sent from a Worker to the Master in response to a termination request.
- **Usage:**
  - **Source:** Worker
  - **Destination:** Master
  - **Utility:** Confirms or denies the success of the termination request.
- **Parameter:**
  - `success: Boolean`: Indicates whether the termination request was successful.

![terminateReply drawio](https://github.com/AlexDevauchelle/434project/assets/70631774/d520a8fa-e734-4918-b16a-79076ef4032a)

## SaveBlockRequest

- **Description:** A message sent from one Worker to another Worker to request saving a block.
- **Usage:**
  - **Source:** Worker
  - **Destination:** Worker
  - **Utility:** Requests the target Worker to save a block.
- **Parameter:**
  - `block: Block`: The block to be saved.
  - `otherBlockToSend: Boolean`: Indicates whether the sending Worker has another block to send to the receiving Worker.

![SaveBlockReq drawio](https://github.com/AlexDevauchelle/434project/assets/70631774/fdab8cdf-c0d5-430f-a25a-10c972c5817e)

## SaveBlockReply

- **Description:** A message sent from a Worker to another Worker in response to a block saving request.
- **Usage:**
  - **Source:** Worker
  - **Destination:** Worker
  - **Utility:** Confirms or denies the success of saving the block.
- **Parameter:**
  - `success: Boolean`: Indicates whether the block saving request was successful.

![terminateReply drawio](https://github.com/AlexDevauchelle/434project/assets/70631774/2f0ec34f-ad35-4f2e-996f-dd163ebcdc0c)

---

This documentation provides an overview of the network message structures used in our project, including their descriptions, types, and usage. It can serve as a reference for understanding the network message structures in the system.
