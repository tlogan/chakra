package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global

object Messenger {

  def props(): Props = {
    Props[Messenger]
  }

  case class SetSocket(socket: Socket)
  case class WriteNextTrackOp(trackOp: Option[Track]) 
  case object Shift
  case class WriteBothTracks(current: Option[Track], next: Option[Track]) 
  case class WritePlayState(play: PlayState)

  val currentPos = 1 
  val nextPos = 2 

}

class Messenger extends Actor {

  import Messenger._

  def receive = receiveSocket()

  def receiveSocket(): Receive = {
    case SetSocket(socket) =>
      val socketInput = socket.getInputStream()
      val dataInput = new DataInputStream(socketInput)
      read(socketInput, dataInput)
      val socketOutput = socket.getOutputStream()
      val dataOutput = new DataOutputStream(socketOutput)
      context.become(receiveWrites(socketOutput, dataOutput))

  }

  def receiveWrites(socketOutput: OutputStream, dataOutput: DataOutputStream): Receive = {
    case WriteNextTrackOp(trackOp) => 
      writeTrack(nextPos, trackOp, socketOutput, dataOutput)

    case Shift =>
      shift()

    case WriteBothTracks(current, next) =>
      writeTrack(currentPos, current, socketOutput, dataOutput)
      writeTrack(nextPos, next, socketOutput, dataOutput)

    case WritePlayState(playState) => 
      writePlayState(playState)

  }

  def shift(): Unit = {
  }


  def writePlayState(playState: PlayState): Unit = {
  }


  def writeTrack(pos: Int, trackOp: Option[Track], 
    socketOutput: OutputStream, dataOutput: DataOutputStream
  ): Unit = {
    Log.d("chakra", "write track pos " + pos)
    trackOp match {
      case None => //dont write anything 
      case Some(track) =>
        try {
          //write position 
          dataOutput.writeLong(pos)

          //write the file path
          dataOutput.writeLong(track.path.length())
          dataOutput.flush()
          socketOutput.write(track.path.getBytes())

          //write the audio data 
          val file = new File(track.path)
          val fileInput = new FileInputStream(file);
          dataOutput.writeLong(file.length())
          dataOutput.flush()
          val buffer = new Array[Byte](512)

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
          case e: IOException => 
            Log.d("chakra", "error writing exception")
        }
    }

  }

  def read(socketInput: InputStream, dataInput: DataInputStream): Unit = {
    Log.d("chakra", "readings tracks from socketInput " + socketInput)
    val f = Future {
      val pos = dataInput.readLong().toInt
      Log.d("chakra", "read track pos " + pos)
      readTrack(pos, socketInput, dataInput)

    } onComplete {
      case Success(_) => read(socketInput, dataInput)
      case Failure(e) => 
        try {
          socketInput.close()
        } catch {
          case e: IOException => e.printStackTrace();
            Log.d("chakra", "error closing socket")
        }
    }

  }

  @throws(classOf[IOException])
  def readTrack(pos: Int, socketInput: InputStream, dataInput: DataInputStream): Unit = {
    //read the track path
    val trackPathSize = dataInput.readLong().toInt
    val trackPathBuffer = new Array[Byte](trackPathSize)
    socketInput.read(trackPathBuffer)
    val path = new String(trackPathBuffer, 0, trackPathSize)
    val track = Track(path, "", "", "")
    pos match {
      case 1 => 
        mainActorRef ! MainActor.SetCurrentRemoteTrack(track)
      case 2 =>
        mainActorRef ! MainActor.SetNextRemoteTrack(track)
      case _ => 
        Log.d("chakra", "remote track error: pos is " + pos)
    }

    //read the track audio data 
    val setRemoteAudio: (Array[Byte]) => Unit = pos match {
      case 1 => 
        (audioBuffer) => 
          mainActorRef ! MainActor.SetCurrentRemoteAudio(audioBuffer)
      case 2 =>
        (audioBuffer) => 
          mainActorRef ! MainActor.SetNextRemoteAudio(audioBuffer)
      case _ => 
        (audioBuffer) => 
          Log.d("chakra", "remote audio error: pos is " + pos)
    }

    val bufferSize = 512 
    val audioBuffer = new Array[Byte](bufferSize)
    var remainingLen = dataInput.readLong()

    var streamAlive = true 
    while (remainingLen > 0 && streamAlive) {
      val maxLen = Math.min(bufferSize, remainingLen).toInt
      val len = socketInput.read(audioBuffer, 0, maxLen)
      if (len != -1) {
        setRemoteAudio(audioBuffer.slice(0, len))
        remainingLen = remainingLen - len;
      } else {
        streamAlive = false
      }
    }

  }

}
