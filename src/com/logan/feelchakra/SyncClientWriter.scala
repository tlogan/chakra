package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global

object SyncClientWriter {

  case class WriteSyncResponse(syncRequestReadTime: Long)

}

import SyncClientWriter._

trait SyncClientWriter {
  this: Actor with SocketWriter =>

  val receiveWriteSyncResponse: Receive = {

    case WriteSyncResponse(syncRequestReadTime) =>
      Log.d("chakra", "writing sync response")
      writeSyncResponse(syncRequestReadTime)

  }

  def writeSyncResponse(syncRequestReadTime: Long): Unit = {
    try {
      //write messageType 
      dataOutput.writeInt(SyncServerReader.SyncResponseMessage)
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


}
