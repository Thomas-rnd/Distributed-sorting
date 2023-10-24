# 434project
CS434 - Course Project

Goal: Distributed & Parralel sorting key/value records stored on multiple machines!

Input specification :
  Record: 100 bytes in length
    first 10 bytes for a key (used to compare records)
    remaining 90 bytes for a value (not used in sortin

  1 master, a fixed number of workers with their IP addresses

  Input blocks of 32MB each on each worker

![image](https://github.com/AlexDevauchelle/434project/assets/70631774/da10cad5-106d-4dbc-bf61-684a4df5229c)



Step by step :

1. Establish connection M(aster) <-> W(orkers)
2. On each W : Compute Sampling of key & send it to M
3. On M : Compute key distribution & Attribute key range to each W (e.g. W_1 -> (1,60), W_2 -> (61,100))
4. On M : Send all key range to all W
5. On each W : Sort (on key) and Partition each block (on key range)
6. On each W : For each partition, send it to its coresponding worker
7. On each W : Merge partitions and sort it



Partition exchange protocol :

How to use :
