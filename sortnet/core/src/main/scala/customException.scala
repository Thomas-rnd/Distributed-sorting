package com.cs434.sortnet.core


class WorkerError(workerIP: String, message: String, cause: Throwable) extends RuntimeException(message, cause) {
  def getWorkerIP: String = workerIP
}

class WorkerFailed(workerIP: String, message: String) extends RuntimeException(message) {
  def getWorkerIP: String = workerIP
}

class MasterTaskError(message: String) extends RuntimeException(message) {}

class WorkerTaskError(message: String) extends RuntimeException(message) {}
