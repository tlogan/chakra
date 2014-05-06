package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global

object Synchronizer {

  case object WriteSyncResult 
  case class WriteTimeDiff(timeDiff: Int) 
  case class SetMeanTimeDiff(timeDiff: Int) 

  val SyncRequestMessage = 50 
  val SyncResultMessage = 60 
  val TimeDiffMessage = 70 

  var _syncRequestWriteTime: Long = 0
  var localTimeDiff: Int = 0
  var foreignTimeDiff: Int = 0

  var localSet = false
  var foreignSet = false
  def meanTimeDiff = (localTimeDiff - foreignTimeDiff)/2


}

import Synchronizer._

class Synchronizer(
    actorRef: ActorRef,
    socketOutput: OutputStream,
    dataOutput: DataOutputStream,
    socketInput: InputStream, 
    dataInput: DataInputStream
) {

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
    actorRef ! WriteTimeDiff(localTimeDiff)
    localSet = true
    if (foreignSet) {
      actorRef ! SetMeanTimeDiff(meanTimeDiff)
    }
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
    foreignSet = true
    if (localSet) {
      actorRef ! SetMeanTimeDiff(meanTimeDiff)
    }
  }

  


}
