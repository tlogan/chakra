package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global

object Messenger {

  def props(): Props = {
    Props[Messenger]
  }

  case class SetSocket(socket: Socket)
  case class WriteTrackOp(trackOp: Option[Track]) 
  case object Shift
  case class WriteBothTracks(current: Option[Track], next: Option[Track]) 
  case class WritePlayState(play: PlayState)

}

class Messenger extends Actor {

  import Messenger._

  def receive = receiveSocket()

  def receiveSocket(): Receive = {
    case SetSocket(socket) =>
      read(socket)
      context.become(receiveWrites(socket))

  }

  def receiveWrites(socket: Socket): Receive = {
    case WriteTrackOp(trackOp) => 
      writeTrack(trackOp, socket)

    case Shift =>
      shift(socket)

    case WriteBothTracks(current, next) =>
      writeTrack(current, socket)
      shift(socket)
      writeTrack(next, socket)

    case WritePlayState(playState) => 
      writePlayState(playState, socket)

  }

  def shift(socket: Socket): Unit = {
  }


  def writePlayState(playState: PlayState, socket: Socket): Unit = {
  }


  def writeTrack(trackOp: Option[Track], socket: Socket): Unit = {
    val socketOutput = socket.getOutputStream()
    val dataOutput = new DataOutputStream(socketOutput)
    trackOp match {
      case None => 
        dataOutput.writeLong(0)
      case Some(track) =>
        dataOutput.writeLong(1)
        try {

          //write the file path
          dataOutput.writeLong(track.path.length())
          dataOutput.flush()
          socketOutput.write(track.path.getBytes())

          //write the file data 
          val file = new File(track.path)
          val fileInput = new FileInputStream(file);
          dataOutput.writeLong(file.length())
          dataOutput.flush()
          val buffer = new Array[Byte](1024)

          var streamAlive = true
          while (streamAlive) {
            val len = fileInput.read(buffer)
            if (len != -1) {
              socketOutput.write(buffer, 0, len);
            } else {
              streamAlive = false
            }
          }
          fileInput.close();

        } catch {
          case e: IOException => e.printStackTrace();
        }
        }

  }



  def read(socket: Socket): Unit = {
    Log.d("chakra", "readings tracks from socket " + socket)
    Future({
      val socketInput = socket.getInputStream()
      val dataInput = new DataInputStream(socketInput)
      while (true) {
        try {
          val readMode = dataInput.readLong().toInt
          readMode match {
            case 0 => reportNoTrack()
            case 1 => readTrack(socketInput, dataInput)
          }
 
        } catch {
          case e: IOException => 
            e.printStackTrace();
            try {
              socket.close();
            } catch {
              case e: IOException => e.printStackTrace();
            }
        }
      }
    })
  }

  def reportNoTrack(): Unit = {
    mainActorRef ! MainActor.SetRemoteTrack(Track("NO TRACK", "", "", ""))
  }

  def readTrack(socketInput: InputStream, dataInput: DataInputStream): Unit = {

    //read the track path
    val trackPathSize = dataInput.readLong().toInt
    val trackPathBuffer = new Array[Byte](trackPathSize)
    socketInput.read(trackPathBuffer)
    val path = new String(trackPathBuffer, 0, trackPathSize)
    mainActorRef ! MainActor.SetRemoteTrack(Track(path, "", "", ""))

    //read the track audio data 
    val bufferSize = 1024
    val audioBuffer = new Array[Byte](bufferSize)
    var remainingLen = dataInput.readLong()

    var streamAlive = true 
    while (remainingLen > 0 && streamAlive) {
      val maxLen = Math.min(bufferSize, remainingLen).toInt
      val len = socketInput.read(audioBuffer, 0, maxLen)
      if (len != -1) {
        mainActorRef ! MainActor.SetRemoteAudio(audioBuffer, len)
        remainingLen = remainingLen - len;
      } else {
        streamAlive = false
      }
    }


  }

}
