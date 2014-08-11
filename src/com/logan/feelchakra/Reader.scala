package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global
import akka.pattern.ask
import akka.util.Timeout

object Reader {

  val SyncResponseMessage = 60 
  val SyncRequestMessage = 50 

  def read(socketInput: InputStream, dataInput: DataInputStream, receive: PartialFunction[Any, Unit]): Unit = {
    val f = Future {
      val messageType = dataInput.readInt()
      try {
        receive(messageType)
      } catch {
        case e: Throwable => 
          Log.d("chakra", "error reading message: " + messageType)
          throw e
      }

    } onComplete {
      case Success(_) => read(socketInput, dataInput, receive)
      case Failure(e) => 
        try {
          socketInput.close()
        } catch {
          case e: IOException => 
            Log.d("chakra", "error closing socket")
            e.printStackTrace()
        } finally {
          Log.d("chakra", "error reading")
          throw e
        }
    }

  }

  def receiveReadSyncRequest(writerRef: ActorRef): PartialFunction[Any, Unit] = {
    PartialFunction[Any, Unit] {
      case SyncRequestMessage =>
        writerRef ! ListenerWriter.WriteSyncResponse(Platform.currentTime)
    }
  }

  def receiveReadSyncResponse(
      socketInput: InputStream, 
      dataInput: DataInputStream,
      writerRef: ActorRef,
      onTimeDiff: Int => Unit
  ): PartialFunction[Any, Unit] = {

    implicit val timeout = Timeout(5000)

    def localTimeDiff(
        syncRequestWriteTime: Long,
        syncRequestReadTime: Long,
        syncResponseWriteTime: Long, 
        syncResponseReadTime: Long 
    ): Int = {
      ((
        syncResponseReadTime + 
        syncRequestWriteTime - 
        syncResponseWriteTime - 
        syncRequestReadTime
      )/2).toInt
    }

    @throws(classOf[IOException])
    def readSyncResponse(): Unit = {
      Log.d("chakra", "reading sync result")

      val syncResponseReadTime = Platform.currentTime
      val syncRequestReadTime = dataInput.readLong()
      val syncResponseWriteTime = dataInput.readLong()

      val f = writerRef ? StationWriter.GetSyncRequestWriteTime
      f.onComplete {
        case Success(syncRequestWriteTime: Long) => 
          Log.d("chakra", "t0 " + syncRequestWriteTime)
          Log.d("chakra", "t1 " + syncRequestReadTime)
          Log.d("chakra", "t2 " + syncResponseWriteTime)
          Log.d("chakra", "t3 " + syncResponseReadTime)

          val timeDiff = localTimeDiff(
              syncRequestWriteTime, syncRequestReadTime,
              syncResponseWriteTime, syncResponseReadTime
          )

          onTimeDiff(timeDiff)

          Log.d("chakra", "LocalTimeDiff: " + timeDiff)

        case Failure(e) =>
          Log.d("chakra", "failed asking for localTimeDiff: " + e.getMessage)
      }

    }

    PartialFunction[Any, Unit] {
      case SyncResponseMessage =>
        readSyncResponse()
    }

  }

}
