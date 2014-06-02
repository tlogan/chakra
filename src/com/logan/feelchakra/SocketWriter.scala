package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global

object SocketWriter {

  case class WriteSyncResponse(syncRequestReadTime: Long)
  case class WriteTimeDiff(timeDiff: Int) 
  case class GetSyncRequestWriteTime

}

import SocketWriter._

trait SocketWriter {
  this: Actor =>

  var socket: Socket = _

  var socketOutput: OutputStream = _
  var dataOutput: DataOutputStream = _

  var syncRequestWriteTime: Long = 0

  val receiveWriteSync: Receive = {

    case WriteSyncResponse(syncRequestReadTime) =>
      Log.d("chakra", "writing sync response")
      writeSyncResponse(syncRequestReadTime)

    case WriteTimeDiff(timeDiff) =>
      Log.d("chakra", "writing time diff")
      writeTimeDiff(timeDiff)

    case GetSyncRequestWriteTime =>
      Log.d("chakra", "getting syncRequestWriteTime")
      sender() ! syncRequestWriteTime

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
      syncRequestWriteTime = Platform.currentTime
      dataOutput.writeInt(SocketReader.SyncRequestMessage)
      dataOutput.flush()
    } catch {
      case e: IOException => 
        Log.d("chakra", "error writing sync request")
        e.printStackTrace()
    }
  }

  def writeSyncResponse(syncRequestReadTime: Long): Unit = {
    try {
      //write messageType 
      dataOutput.writeInt(SocketReader.SyncResponseMessage)
      dataOutput.flush()

      //write the request read time 
      dataOutput.writeLong(syncRequestReadTime)
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
