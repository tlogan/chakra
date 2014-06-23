package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global

object ListenerWriter {

  def props(): Props = {
    Props[ListenerWriter]
  }

  case class SetSocket(socket: Socket)
  case class WriteTrackOp(trackOp: Option[Track]) 
  case class WriteAudioBuffer(path: String, audioBuffer: Array[Byte]) 
  case class WriteAudioDone(path: String)


  case class WriteCurrentTrackPath(path: String)
  case class WritePlayState(playState: PlayState) 

  case class WriteSyncResponse(syncRequestReadTime: Long)

}

import ListenerWriter._

class ListenerWriter extends Actor {

  def receive: Receive = {

    def receiveWrite(socketOutput: OutputStream, dataOutput: DataOutputStream): Receive = {

      def writeSyncResponse(syncRequestReadTime: Long): Unit = {
        try {
          //write messageType 
          dataOutput.writeInt(Runnable.SyncResponseMessage)
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

      def writeTrack(trackOp: Option[Track]): Unit = {
        Log.d("chakra", "write track")
        trackOp match {
          case None => //dont write anything 
          case Some(track) =>
            try {

              //write messageType 
              dataOutput.writeInt(Runnable.TrackMessage)
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

      def writeCurrentTrackPath(path: String): Unit = {
        Log.d("chakra", "write current track path")

        try {
          //write messageType 
          dataOutput.writeInt(Runnable.TrackPathMessage)
          dataOutput.flush()

          //write data
          dataOutput.writeInt(path.length())
          dataOutput.flush()
          socketOutput.write(path.getBytes())

        } catch {
          case e: IOException => 
            Log.d("chakra", "error writing audioPlayState")
        }
      }

      def writePlayState(playState: PlayState): Unit = {
        Log.d("chakra", "write play state")

        try {

          //write messageType 
          dataOutput.writeInt(Runnable.PlayStateMessage)
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

      def writeAudioBuffer(path: String, audioBuffer: Array[Byte]): Unit = {
        try {

          //write messageType 
          dataOutput.writeInt(Runnable.AudioBufferMessage)
          dataOutput.flush()

          //write data
          dataOutput.writeInt(path.length())
          dataOutput.flush()
          socketOutput.write(path.getBytes())

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

      def writeAudioDone(path: String): Unit = {
        Log.d("chakra", "write audio done")

        try {

          //write messageType 
          dataOutput.writeInt(Runnable.AudioDoneMessage)
          dataOutput.flush()

          //write data
          dataOutput.writeInt(path.length())
          dataOutput.flush()
          socketOutput.write(path.getBytes())

        } catch {
          case e: IOException => 
            Log.d("chakra", "error writing audio done")
            e.printStackTrace()
        }
      }

      PartialFunction[Any, Unit] {
        case WriteSyncResponse(syncRequestReadTime) =>
          Log.d("chakra", "writing sync response")
          writeSyncResponse(syncRequestReadTime)

        case WriteTrackOp(trackOp) =>
          writeTrack(trackOp)

        case WriteAudioBuffer(path, audioBuffer) =>
          writeAudioBuffer(path, audioBuffer)

        case WriteAudioDone(path) =>
          writeAudioDone(path)

        case WriteCurrentTrackPath(path) =>
          writeCurrentTrackPath(path)

        case WritePlayState(playState) =>
          writePlayState(playState)
      }

    }

    PartialFunction[Any, Unit] {
      case SetSocket(socket) =>
        val socketOutput = socket.getOutputStream()
        val dataOutput = new DataOutputStream(socketOutput)
        Log.d("chakra", "setting socket in ListenerWriter")
        context.become(receiveWrite(socketOutput, dataOutput))
    }
  }

}
