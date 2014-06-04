package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global

object SyncServerWriter {

  case class GetSyncRequestWriteTime
  case class WriteSyncRequest

}

import SyncServerWriter._

trait SyncServerWriter {
  this: Actor with SocketWriter =>

  var syncRequestWriteTime: Long = 0

  val receiveGetSyncRequest: Receive = {

    case GetSyncRequestWriteTime =>
      Log.d("chakra", "getting syncRequestWriteTime")
      sender() ! syncRequestWriteTime

    case WriteSyncRequest =>
      writeSyncRequest()

  }

  def writeSyncRequest(): Unit = {
    Log.d("chakra", "writing sync request")
    try {
      //write messageType 
      syncRequestWriteTime = Platform.currentTime
      dataOutput.writeInt(SyncClientReader.SyncRequestMessage)
      dataOutput.flush()
    } catch {
      case e: IOException => 
        Log.d("chakra", "error writing sync request")
        e.printStackTrace()
    }
  }

}
