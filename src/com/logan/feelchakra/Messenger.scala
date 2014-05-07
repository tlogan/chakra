package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global

object Messenger {

  case object WriteSyncResult 
  case class WriteTimeDiff(timeDiff: Int) 

  val SyncRequestMessage = 50 
  val SyncResultMessage = 60 
  val TimeDiffMessage = 70 


}

import Messenger._

trait Messenger {
  this: Actor =>

  var socket: Socket = _

  var socketInput: InputStream = _
  var dataInput: DataInputStream = _
  var socketOutput: OutputStream = _
  var dataOutput: DataOutputStream = _


  var _syncRequestWriteTime: Long = 0
  var localTimeDiff: Int = 0
  var foreignTimeDiff: Int = 0


  def meanTimeDiff = (localTimeDiff - foreignTimeDiff)/2

  val receiveWriteSync: Receive = {

    case WriteSyncResult =>
      writeSyncResult()

    case WriteTimeDiff(timeDiff) =>
      writeTimeDiff(timeDiff)

  }


  def setSocket(socket: Socket): Unit = {
    this.socket = socket
    socketInput = socket.getInputStream()
    dataInput = new DataInputStream(socketInput)
    socketOutput = socket.getOutputStream()
    dataOutput = new DataOutputStream(socketOutput)
  }

  def writeSyncRequest(): Unit = {
    Log.d("chakra", "writing sync request")
    try {
      //write messageType 
      dataOutput.writeInt(SyncRequestMessage)
      dataOutput.flush()
      _syncRequestWriteTime = Platform.currentTime
    } catch {
      case e: IOException => 
        Log.d("chakra", "error writing sync request")
    }
  }

  @throws(classOf[IOException])
  def readSyncResult(): Unit = {
    Log.d("chakra", "reading sync result")

    val syncResultReadTime = Platform.currentTime
    val otherTime = dataInput.readLong()
    localTimeDiff = ((syncResultReadTime + _syncRequestWriteTime)/2 - otherTime).toInt
    self ! WriteTimeDiff(localTimeDiff)
    Log.d("chakra", "Time Diff: " + localTimeDiff)
  }


  def writeSyncResult(): Unit = {

    Log.d("chakra", "writing sync result")
    try {
      //write messageType 
      dataOutput.writeInt(SyncResultMessage)
      dataOutput.flush()

      //write the current time 
      dataOutput.writeLong(Platform.currentTime)
      dataOutput.flush()
    } catch {
      case e: IOException => 
        Log.d("chakra", "error writing sync result")
    }
  }

  def writeTimeDiff(timeDiff: Int): Unit = {
    Log.d("chakra", "writing local time Diff")
    try {
      //write messageType 
      dataOutput.writeInt(TimeDiffMessage)
      dataOutput.flush()

      //write the current time 
      dataOutput.writeInt(timeDiff)
      dataOutput.flush()
    } catch {
      case e: IOException => 
        Log.d("chakra", "error writing time diff")
    }
  }

  @throws(classOf[IOException])
  def readTimeDiff(): Unit = {
    Log.d("chakra", "reading time diff")
    foreignTimeDiff = dataInput.readInt()
    Log.d("chakra", "foreign time diff: " + foreignTimeDiff)
  }

  


}
