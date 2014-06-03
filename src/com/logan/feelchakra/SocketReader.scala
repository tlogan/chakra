package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global
import akka.pattern.ask
import akka.util.Timeout

object SocketReader {

  val SyncRequestMessage = 50 
  val SyncResponseMessage = 60 
  val TimeDiffMessage = 70 


}

import SocketReader._

abstract class SocketReader(socket: Socket, writerRef: ActorRef) {

  implicit val timeout = Timeout(5000)

  var syncRequestWriteTime: Long = 0

  var syncRequestReadTime: Long = 0
  var syncResponseWriteTime: Long = 0 

  var syncResponseReadTime: Long = 0 

  def localTimeDiff = (
    (syncResponseReadTime + syncRequestWriteTime - syncResponseWriteTime - syncRequestReadTime)
    /2
  ).toInt

  var foreignTimeDiff: Int = 0

  def meanTimeDiff = if (foreignTimeDiff != 0) (localTimeDiff - foreignTimeDiff)/2 else localTimeDiff

  var socketInput: InputStream = socket.getInputStream()
  var dataInput: DataInputStream = new DataInputStream(socketInput)

  def read(): Unit = {
    val f = Future {
      val messageType = dataInput.readInt()

      if (receive.isDefinedAt(messageType)) {
        receive(messageType)
      } else {
        Log.d("chakra", "read problem: not defined at " + messageType)
      }

    } onComplete {
      case Success(_) => read()
      case Failure(e) => 
        try {
          socketInput.close()
          Log.d("chakra", "read fail, closing socket")
          e.printStackTrace()
        } catch {
          case e: IOException => 
            Log.d("chakra", "error closing socket")
            e.printStackTrace()
        }
    }

  }

  def setSocket(socket: Socket): Unit = {
  }

  def receive: PartialFunction[Any, Unit] 

  val receiveReadSync: PartialFunction[Any, Unit] = {

    case SyncResponseMessage =>
      readSyncResponse()

    case SyncRequestMessage =>
      writerRef ! SocketWriter.WriteSyncResponse(Platform.currentTime)

    case TimeDiffMessage =>
      readTimeDiff()

  }

  @throws(classOf[IOException])
  def readSyncResponse(): Unit = {
    Log.d("chakra", "reading sync result")

    this.syncResponseReadTime = Platform.currentTime

    this.syncRequestReadTime = dataInput.readLong()

    this.syncResponseWriteTime = dataInput.readLong()

    val f = writerRef ? SocketWriter.GetSyncRequestWriteTime
    f.onComplete {
      case Success(syncRequestWriteTime: Long) => 
        this.syncRequestWriteTime = syncRequestWriteTime

        Log.d("chakra", "t0 " + syncRequestWriteTime)
        Log.d("chakra", "t1 " + syncRequestReadTime)
        Log.d("chakra", "t2 " + syncResponseWriteTime)
        Log.d("chakra", "t3 " + syncResponseReadTime)
        Log.d("chakra", "LocalTimeDiff: " + localTimeDiff)
        writerRef ! SocketWriter.WriteTimeDiff(localTimeDiff)

      case Failure(e) =>
        Log.d("chakra", "failed asking for localTimeDiff: " + e.getMessage)
    }

  }



  @throws(classOf[IOException])
  def readTimeDiff(): Unit = {
    Log.d("chakra", "reading foreign time diff")
    foreignTimeDiff = dataInput.readInt()
    Log.d("chakra", "foreign time diff: " + foreignTimeDiff)
  }

}
