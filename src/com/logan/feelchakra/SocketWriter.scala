package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global

object SocketWriter {
}

import SocketWriter._

trait SocketWriter {
  this: Actor =>

  var socket: Socket = _

  var socketOutput: OutputStream = _
  var dataOutput: DataOutputStream = _

  def setSocket(socket: Socket): Unit = {
    this.socket = socket
    socketOutput = socket.getOutputStream()
    dataOutput = new DataOutputStream(socketOutput)
  }

}
