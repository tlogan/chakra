package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global

object StationWriter {

  def props(): Props = {
    Props[StationWriter]
  }

  case class SetSocket(socket: Socket)

  case class GetSyncRequestWriteTime
  case class WriteSyncRequest

}

import StationWriter._

class StationWriter extends Actor {

  def receive = {

    var syncRequestWriteTime: Long = 0

    def writeSyncRequest(socketOutput: OutputStream,dataOutput: DataOutputStream): Unit = {
      Log.d("chakra", "writing sync request")
      try {
        //write messageType 
        syncRequestWriteTime = Platform.currentTime
        dataOutput.writeInt(Reader.SyncRequestMessage)
        dataOutput.flush()
      } catch {
        case e: IOException => 
          Log.d("chakra", "error writing sync request")
          e.printStackTrace()
      }
    }

    PartialFunction[Any, Unit] {
      case SetSocket(socket) => 
        Log.d("chakra", "setting socket in station writer")
        val socketOutput = socket.getOutputStream()
        val dataOutput = new DataOutputStream(socketOutput)
        writeSyncRequest(socketOutput, dataOutput)

      case GetSyncRequestWriteTime =>
        Log.d("chakra", "getting syncRequestWriteTime")
        sender() ! syncRequestWriteTime
    }

  }

}
