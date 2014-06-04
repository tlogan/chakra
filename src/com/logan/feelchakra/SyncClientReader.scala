package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global

import akka.pattern.ask
import akka.util.Timeout

object SyncClientReader {

  val SyncRequestMessage = 50 

}

import SyncClientReader._

trait SyncClientReader {
  this: SocketReader =>

  val receiveReadSyncRequest: PartialFunction[Any, Unit] = {

    case SyncRequestMessage =>
      writerRef ! SyncClientWriter.WriteSyncResponse(Platform.currentTime)

  }

}
