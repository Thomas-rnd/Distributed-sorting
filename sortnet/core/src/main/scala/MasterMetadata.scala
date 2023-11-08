package com.cs434.sortnet.core

import java.net.Socket

// Define the WorkerMetadata case class
case class MasterMetadata(ip: String, port: Int, socket: Socket)
