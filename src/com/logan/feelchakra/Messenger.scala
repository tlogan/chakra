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
  case object WriteAudioDone 
  case class WritePlayState(playState: PlayState) 
  case object WriteSyncResult 
  case class WriteLocalTimeDiff(timeDiff: Int) 
  case class SetMeanTimeDiff(timeDiff: Int) 

  val TrackMessage = 10
  val PlayStateMessage = 20
  val AudioBufferMessage = 33 
  val AudioDoneMessage = 40 

  val SyncRequestMessage = 50 
  val SyncResultMessage = 60 
  val TimeDiffMessage = 70 

  var _syncRequestWriteTime: Long = 0
  var localTimeDiff: Int = 0
  var meanTimeDiff: Int = 0

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

      writeSyncRequest(socketOutput, dataOutput)
      context.become(receiveWrites(socketOutput, dataOutput))

  }

  def receiveWrites(socketOutput: OutputStream, dataOutput: DataOutputStream): Receive = {

    case WriteTrackOp(trackOp) =>
      writeTrack(trackOp, socketOutput, dataOutput)

    case WriteAudioBuffer(audioBuffer) =>
      writeAudioBuffer(audioBuffer, socketOutput, dataOutput)

    case WriteAudioDone =>
      writeAudioDone(socketOutput, dataOutput)

    case WritePlayState(playState) =>
      writePlayState(playState, socketOutput, dataOutput)

    case WriteSyncResult =>
      writeSyncResult(socketOutput, dataOutput)

    case WriteLocalTimeDiff(timeDiff) =>
      writeLocalTimeDiff(timeDiff, socketOutput, dataOutput)

    case SetMeanTimeDiff(timeDiff) =>
      meanTimeDiff = timeDiff
      Log.d("chakra", "Mean Time Diff: " + meanTimeDiff)

  }

  def writeSyncRequest(socketOutput: OutputStream, dataOutput: DataOutputStream): Unit = {
    Log.d("chakra", "writing sync request")
    try {
      //write messageType 
      dataOutput.writeInt(SyncRequestMessage)
      dataOutput.flush()
      _syncRequestWriteTime = Platform.currentTime
    } catch {
      case e: IOException => 
        Log.d("chakra", "error writing sync request")
    }
  }

  def writeSyncResult(socketOutput: OutputStream, dataOutput: DataOutputStream): Unit = {

    Log.d("chakra", "writing sync result")
    try {
      //write messageType 
      dataOutput.writeInt(SyncResultMessage)
      dataOutput.flush()

      //write the current time 
      dataOutput.writeLong(Platform.currentTime)
      dataOutput.flush()
    } catch {
      case e: IOException => 
        Log.d("chakra", "error writing sync result")
    }
  }

  def writeLocalTimeDiff(timeDiff: Int, socketOutput: OutputStream, dataOutput: DataOutputStream): Unit = {
    Log.d("chakra", "writing local time Diff")
    try {
      //write messageType 
      dataOutput.writeInt(TimeDiffMessage)
      dataOutput.flush()

      //write the current time 
      dataOutput.writeInt(timeDiff)
      dataOutput.flush()
    } catch {
      case e: IOException => 
        Log.d("chakra", "error writing time diff")
    }
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

  def writeAudioDone(socketOutput: OutputStream, dataOutput: DataOutputStream
  ): Unit = {
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
    val f = Future {
      val messageType = dataInput.readInt()
      messageType match {
        case TrackMessage =>
          readTrack(socketInput, dataInput)
        case PlayStateMessage =>
          readPlayState(socketInput, dataInput)
        case AudioBufferMessage =>
          readAudioBuffer(socketInput, dataInput)
        case AudioDoneMessage =>
          mainActorRef ! MainActor.EndStationAudioBuffer
        case SyncRequestMessage =>
          self ! WriteSyncResult
        case SyncResultMessage =>
          readSyncResult(socketInput, dataInput)
        case TimeDiffMessage =>
          readTimeDiff(socketInput, dataInput)
        case i: Int =>
          //Log.d("chakra", "not a valid message type: " + i)
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
  def readSyncResult(socketInput: InputStream, dataInput: DataInputStream): Unit = {
    Log.d("chakra", "reading sync result")

    val syncResultReadTime = Platform.currentTime
    val otherTime = dataInput.readLong()
    localTimeDiff = ((syncResultReadTime + _syncRequestWriteTime)/2 - otherTime).toInt
    self ! WriteLocalTimeDiff(localTimeDiff)
    Log.d("chakra", "Time Diff: " + localTimeDiff)
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

    mainActorRef ! MainActor.SetStationTrack(track)

  }

  @throws(classOf[IOException])
  def readAudioBuffer(socketInput: InputStream, dataInput: DataInputStream): Unit = {

    //read the track path
    val bufferSize = dataInput.readInt()
    val audioBuffer = new Array[Byte](bufferSize)
    socketInput.read(audioBuffer)

    mainActorRef ! MainActor.AddStationAudioBuffer(audioBuffer)

  }

  @throws(classOf[IOException])
  def readPlayState(socketInput: InputStream, dataInput: DataInputStream): Unit = {
    Log.d("chakra", "reading playState ")
    val startTime = dataInput.readLong()
    val playState = if (startTime > 0) {

      Log.d("chakra", "reading playState with meanTimeDiff: " + meanTimeDiff)
      Playing(startTime + meanTimeDiff)
    } else {
      NotPlaying
    }

    mainActorRef ! MainActor.SetStationPlayState(playState)

  }

  @throws(classOf[IOException])
  def readTimeDiff(socketInput: InputStream, dataInput: DataInputStream): Unit = {
    Log.d("chakra", "reading time diff")
    val timeDiff = (localTimeDiff - dataInput.readInt())/2
    self ! SetMeanTimeDiff(timeDiff)
  }


}
