package com.cs434.sortnet.core


class WorkerError(workerIP: String, message: String) extends Exception(message) {
  def getWorkerIP: String = workerIP
}

class WorkerFailed(workerIP: String, message: String) extends Exception(message) {
  def getWorkerIP: String = workerIP
}

class MasterTaskError(message: String) extends Exception(message) {}

class WorkerTaskError(message: String) extends Exception(message) {}
