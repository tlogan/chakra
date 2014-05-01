package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global

object Messenger {

  def props(): Props = {
    Props[Messenger]
  }

  case class SetSocket(socket: Socket)
  case class WriteTrackOp(trackOp: Option[Track]) 
  case class WriteAudioBuffer(audioBuffer: Array[Byte]) 
  case class WritePlayState(playState: PlayState) 

  val TrackMessage = 10
  val PlayStateMessage = 20
  val AudioBufferMessage = 30 

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

    case WriteTrackOp(trackOp) =>
      writeTrack(trackOp, socketOutput, dataOutput)

    case WriteAudioBuffer(audioBuffer) =>
      writeAudioBuffer(audioBuffer, socketOutput, dataOutput)

    case WritePlayState(playState) =>
      writePlayState(playState, socketOutput, dataOutput)

  }

  def writeTrack(trackOp: Option[Track], socketOutput: OutputStream, dataOutput: DataOutputStream
  ): Unit = {
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

  def writeAudioBuffer(audioBuffer: Array[Byte], socketOutput: OutputStream, dataOutput: DataOutputStream
  ): Unit = {
    Log.d("chakra", "write audio buffer")

    try {

      //write messageType 
      dataOutput.writeInt(AudioBufferMessage)
      dataOutput.flush()

      dataOutput.writeInt(audioBuffer.length)
      dataOutput.flush()
      socketOutput.write(audioBuffer)

    } catch {
      case e: IOException => 
        Log.d("chakra", "error writing audioBuffer")
    }
  }

  def writePlayState(playState: PlayState, socketOutput: OutputStream, dataOutput: DataOutputStream
  ): Unit = {
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


  def read(socketInput: InputStream, dataInput: DataInputStream): Unit = {
    Log.d("chakra", "readings from socketInput " + socketInput)
    val f = Future {
      val messageType = dataInput.readInt()
      Log.d("chakra", "messageType: " + messageType)
      messageType match {
        case TrackMessage =>
          readTrack(socketInput, dataInput)
        case PlayStateMessage =>
          readPlayState(socketInput, dataInput)
        case AudioBufferMessage =>
          readAudioBuffer(socketInput, dataInput)
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
  def readTrack(socketInput: InputStream, dataInput: DataInputStream): Unit = {
    Log.d("chakra", "reading track ")

    //read the track path
    val trackPathSize = dataInput.readInt()
    val trackPathBuffer = new Array[Byte](trackPathSize)
    socketInput.read(trackPathBuffer)
    val path = new String(trackPathBuffer, 0, trackPathSize)
    val track = Track(path, "", "", "")

  }

  @throws(classOf[IOException])
  def readAudioBuffer(socketInput: InputStream, dataInput: DataInputStream): Unit = {
    Log.d("chakra", "reading audio buffer")

    //read the track path
    val bufferSize = dataInput.readInt()
    val audioBuffer = new Array[Byte](bufferSize)
    socketInput.read(audioBuffer)

  }

  @throws(classOf[IOException])
  def readPlayState(socketInput: InputStream, dataInput: DataInputStream): Unit = {
    Log.d("chakra", "reading playState ")
    val startTime = dataInput.readLong()

  }


}
