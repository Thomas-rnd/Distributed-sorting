package com.cs434.sortnet.core

import java.net.Socket

// Define the WorkerMetadata case class
case class WorkerMetadata(ip: String, port: Int, socket: Socket, keyRange: Option[KeyRange])
