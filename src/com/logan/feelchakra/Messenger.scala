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
  case class WritePlayState(playState: PlayState)

  val currentPos = 1 
  val nextPos = 2 

  val trackMessage = 10
  val playStateMessage = 20

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
      Log.d("chakra", "writing play state")
      //write messageType 
      dataOutput.writeLong(playStateMessage)

  }

  def shift(): Unit = {
  }


  def writeTrack(pos: Int, trackOp: Option[Track], 
    socketOutput: OutputStream, dataOutput: DataOutputStream
  ): Unit = {
    Log.d("chakra", "write track pos " + pos)
    trackOp match {
      case None => //dont write anything 
      case Some(track) =>
        try {

          //write messageType 
          dataOutput.writeLong(trackMessage)
          dataOutput.flush()
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
      val messageType = dataInput.readLong().toInt
      messageType match {
        case trackMessage =>
          readTrack(socketInput, dataInput)
        case playStateMessage =>
          readPlayState(socketInput, dataInput)
        case i: Int =>
          Log.d("chakra", "not a valid message type: " + i)
      }

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
  def readPlayState(socketInput: InputStream, dataInput: DataInputStream): Unit = {
    Log.d("chakra", "reading playState ")
  }

  @throws(classOf[IOException])
  def readTrack(socketInput: InputStream, dataInput: DataInputStream): Unit = {
    Log.d("chakra", "reading track ")

    val pos = dataInput.readLong().toInt

    //read the track path
    val trackPathSize = dataInput.readLong().toInt
    val trackPathBuffer = new Array[Byte](trackPathSize)
    socketInput.read(trackPathBuffer)
    val path = new String(trackPathBuffer, 0, trackPathSize)
    val track = Track(path, "", "", "")

    pos match {
      case currentPos =>
        mainActorRef ! MainActor.SetCurrentRemoteTrack(track)
      case nextPos =>
        mainActorRef ! MainActor.SetNextRemoteTrack(track)
      case _ =>
        Log.d("chakra", "remotePos is not valid: " + pos)
    }

    //read the track audio data 
    val audioSize = dataInput.readLong().toInt


    val bufferSize = 512 
    val audioBuffer = new Array[Byte](bufferSize)
    var remainingLen = audioSize 

    var streamAlive = true 
    while (remainingLen > 0 && streamAlive) {
      val maxLen = Math.min(bufferSize, remainingLen).toInt
      val len = socketInput.read(audioBuffer, 0, maxLen)
      if (len != -1) {
        mainActorRef ! MainActor.SetRemoteAudio(audioBuffer.slice(0, len))
        remainingLen = remainingLen - len;
      } else {
        streamAlive = false
      }
    }

    mainActorRef ! MainActor.SetRemoteAudioDone

  }

}
