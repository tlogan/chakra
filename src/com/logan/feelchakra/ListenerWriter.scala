package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global

object ListenerWriter {

  def props(): Props = {
    Props[ListenerWriter]
  }

  case class SetSocket(socket: Socket)
  case class WriteTrackOp(trackOp: Option[Track]) 
  case class WriteAudioBuffer(audioBuffer: Array[Byte]) 
  case object WriteAudioDone 
  case class WritePlayState(playState: PlayState) 

}

import ListenerWriter._
import SocketWriter._

class ListenerWriter extends Actor with SocketWriter with SyncClientWriter {

  def receive = receiveWrite orElse receiveWriteSyncResponse

  def receiveWrite: Receive = {

    case SetSocket(socket) =>
      setSocket(socket)
      Log.d("chakra", "setting socket in ListenerWriter")

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
          dataOutput.writeInt(StationReader.TrackMessage)
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

  def writePlayState(playState: PlayState): Unit = {
    Log.d("chakra", "write play state")

    try {

      //write messageType 
      dataOutput.writeInt(StationReader.PlayStateMessage)
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

  def writeAudioBuffer(audioBuffer: Array[Byte]): Unit = {
    try {

      //write messageType 
      dataOutput.writeInt(StationReader.AudioBufferMessage)
      dataOutput.flush()

      //write buffer 
      dataOutput.writeInt(audioBuffer.length)
      dataOutput.flush()
      socketOutput.write(audioBuffer, 0, audioBuffer.length)

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
      dataOutput.writeInt(StationReader.AudioDoneMessage)
      dataOutput.flush()

    } catch {
      case e: IOException => 
        Log.d("chakra", "error writing audio done")
        e.printStackTrace()
    }
  }


}
