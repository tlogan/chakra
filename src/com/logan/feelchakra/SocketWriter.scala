package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global

object SocketWriter {

  case object WriteSyncResult 
  case class WriteTimeDiff(timeDiff: Int) 

}

import SocketWriter._

trait SocketWriter {
  this: Actor =>

  var socket: Socket = _

  var socketOutput: OutputStream = _
  var dataOutput: DataOutputStream = _

  var _syncRequestWriteTime: Long = 0

  val receiveWriteSync: Receive = {

    case WriteSyncResult =>
      writeSyncResult()

    case WriteTimeDiff(timeDiff) =>
      writeTimeDiff(timeDiff)

  }

  def setSocket(socket: Socket): Unit = {
    this.socket = socket
    socketOutput = socket.getOutputStream()
    dataOutput = new DataOutputStream(socketOutput)
  }

  def writeSyncRequest(): Unit = {
    Log.d("chakra", "writing sync request")
    try {
      //write messageType 
      dataOutput.writeInt(SocketReader.SyncRequestMessage)
      dataOutput.flush()
      _syncRequestWriteTime = Platform.currentTime
    } catch {
      case e: IOException => 
        Log.d("chakra", "error writing sync request")
        e.printStackTrace()
    }
  }


  def writeSyncResult(): Unit = {

    Log.d("chakra", "writing sync result")
    try {
      //write messageType 
      dataOutput.writeInt(SocketReader.SyncResultMessage)
      dataOutput.flush()

      //write the current time 
      dataOutput.writeLong(Platform.currentTime)
      dataOutput.flush()
    } catch {
      case e: IOException => 
        Log.d("chakra", "error writing sync result")
        e.printStackTrace()
    }
  }


  def writeTimeDiff(timeDiff: Int): Unit = {
    Log.d("chakra", "writing local time Diff")
    try {
      //write messageType 
      dataOutput.writeInt(SocketReader.TimeDiffMessage)
      dataOutput.flush()

      //write the current time 
      dataOutput.writeInt(timeDiff)
      dataOutput.flush()
    } catch {
      case e: IOException => 
        Log.d("chakra", "error writing time diff")
        e.printStackTrace()
    }
  }



  


}
