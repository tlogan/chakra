package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global

object ListenerMessenger {

  def props(): Props = {
    Props[ListenerMessenger]
  }

  case class SetSocket(socket: Socket)
  case class WriteTrackOp(trackOp: Option[Track]) 
  case class WriteAudioBuffer(audioBuffer: Array[Byte]) 
  case object WriteAudioDone 
  case class WritePlayState(playState: PlayState) 


  val TrackMessage = 10
  val PlayStateMessage = 20
  val AudioBufferMessage = 33 
  val AudioDoneMessage = 40 


}

import ListenerMessenger._
import Messenger._

class ListenerMessenger extends Actor with Messenger {

  def receive = receiveSocket()

  def receiveSocket(): Receive = {
    case SetSocket(socket) =>
      setSocket(socket)

      read()

      writeSyncRequest()
      context.become(receiveWrite orElse receiveWriteSync)

  }

  def receiveWrite: Receive = {

    case WriteTrackOp(trackOp) =>
      writeTrack(trackOp)

    case WriteAudioBuffer(audioBuffer) =>
      writeAudioBuffer(audioBuffer)

    case WriteAudioDone =>
      writeAudioDone()

    case WritePlayState(playState) =>
      writePlayState(playState)

  }

  def writeTrack(trackOp: Option[Track]): Unit = {
    Log.d("chakra", "write track")
    trackOp match {
      case None => //dont write anything 
      case Some(track) =>
        try {

          //write messageType 
          dataOutput.writeInt(TrackMessage)
          dataOutput.flush()

          //write the file path
          dataOutput.writeInt(track.path.length())
          dataOutput.flush()
          socketOutput.write(track.path.getBytes())

        } catch {
          case e: IOException => 
            Log.d("chakra", "error writing exception")
        }
    }

  }

  def writeAudioBuffer(audioBuffer: Array[Byte]): Unit = {
    try {

      //write messageType 
      dataOutput.writeInt(AudioBufferMessage)
      dataOutput.flush()

      //write buffer 
      dataOutput.writeInt(audioBuffer.length)
      dataOutput.flush()
      socketOutput.write(audioBuffer)

    } catch {
      case e: IOException => 
        Log.d("chakra", "error writing audioBuffer")
        e.printStackTrace()
    }
  }

  def writeAudioDone(): Unit = {
    Log.d("chakra", "write audio done")

    try {

      //write messageType 
      dataOutput.writeInt(AudioDoneMessage)
      dataOutput.flush()

    } catch {
      case e: IOException => 
        Log.d("chakra", "error writing audio done")
    }
  }

  def writePlayState(playState: PlayState): Unit = {
    Log.d("chakra", "write play state")

    try {

      //write messageType 
      dataOutput.writeInt(PlayStateMessage)
      dataOutput.flush()

      //write data
      val startTime = playState match {
        case Playing(startTime) => startTime
        case NotPlaying => 0
      }
      dataOutput.writeLong(startTime)
      dataOutput.flush()

    } catch {
      case e: IOException => 
        Log.d("chakra", "error writing audioPlayState")
    }
  }

  def read(): Unit = {
    val f = Future {
      val messageType = dataInput.readInt()
      messageType match {
        case SyncResultMessage =>
          readSyncResult()

        case SyncRequestMessage =>
          self ! WriteSyncResult

        case i: Int =>
          //Log.d("chakra", "not a valid message type: " + i)
      }

    } onComplete {
      case Success(_) => read()
      case Failure(e) => 
        try {
          socketInput.close()
        } catch {
          case e: IOException => e.printStackTrace();
            Log.d("chakra", "error closing socket")
        }
    }

  }

}
