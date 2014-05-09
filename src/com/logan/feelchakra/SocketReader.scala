package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global
import akka.pattern.ask
import akka.util.Timeout

object SocketReader {

  val SyncRequestMessage = 50 
  val SyncResultMessage = 60 
  val TimeDiffMessage = 70 


}

import SocketReader._

abstract class SocketReader(socket: Socket, writerRef: ActorRef) {

  implicit val timeout = Timeout(5000)

  var localTimeDiff: Int = 0
  var foreignTimeDiff: Int = 0
  def meanTimeDiff = (localTimeDiff - foreignTimeDiff)/2

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

    case SyncResultMessage =>
      readSyncResult()

    case SyncRequestMessage =>
      writerRef ! SocketWriter.WriteSyncResult

    case TimeDiffMessage =>
      readTimeDiff()

  }

  @throws(classOf[IOException])
  def readSyncResult(): Unit = {
    Log.d("chakra", "reading sync result")

    val syncResultReadTime = Platform.currentTime
    writerRef ! SocketWriter.SetSyncResultReadTime(syncResultReadTime)
    val syncResult = dataInput.readLong()
    writerRef ! SocketWriter.SetSyncResult(syncResult)
    val f = writerRef ? SocketWriter.GetLocalTimeDiff
    f.onComplete {
      case Success(localTimeDiff: Int) => 
        Log.d("chakra", "setting localTimeDiff in reader")
        this.localTimeDiff = localTimeDiff

      case Failure(e) =>
        Log.d("chakra", "failed asking for localTimeDiff: " + e.getMessage)
    }

  }



  @throws(classOf[IOException])
  def readTimeDiff(): Unit = {
    Log.d("chakra", "reading time diff")
    foreignTimeDiff = dataInput.readInt()
    Log.d("chakra", "foreign time diff: " + foreignTimeDiff)
  }
}
