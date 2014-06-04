package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global

import akka.pattern.ask
import akka.util.Timeout

object SyncServerReader {

  val SyncResponseMessage = 60 

}

import SyncServerReader._

trait SyncServerReader {
  this: SocketReader =>

  implicit val timeout = Timeout(5000)

  var timeDiff: Int = 0

  def localTimeDiff(
      syncRequestWriteTime: Long,
      syncRequestReadTime: Long,
      syncResponseWriteTime: Long, 
      syncResponseReadTime: Long 
  ): Int = {
    (
      (syncResponseReadTime + syncRequestWriteTime - syncResponseWriteTime - syncRequestReadTime)
      /2
    ).toInt
  }

  val receiveReadSyncResponse: PartialFunction[Any, Unit] = {

    case SyncResponseMessage =>
      readSyncResponse()

  }

  @throws(classOf[IOException])
  def readSyncResponse(): Unit = {
    Log.d("chakra", "reading sync result")

    val syncResponseReadTime = Platform.currentTime
    val syncRequestReadTime = dataInput.readLong()
    val syncResponseWriteTime = dataInput.readLong()

    val f = writerRef ? SyncServerWriter.GetSyncRequestWriteTime
    f.onComplete {
      case Success(syncRequestWriteTime: Long) => 
        Log.d("chakra", "t0 " + syncRequestWriteTime)
        Log.d("chakra", "t1 " + syncRequestReadTime)
        Log.d("chakra", "t2 " + syncResponseWriteTime)
        Log.d("chakra", "t3 " + syncResponseReadTime)

        timeDiff = localTimeDiff(syncRequestWriteTime, syncRequestReadTime,
          syncResponseWriteTime, syncResponseReadTime)

        Log.d("chakra", "LocalTimeDiff: " + timeDiff)

      case Failure(e) =>
        Log.d("chakra", "failed asking for localTimeDiff: " + e.getMessage)
    }

  }

}
