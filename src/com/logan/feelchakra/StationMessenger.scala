package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global

object StationMessenger {

  def props(): Props = {
    Props[StationMessenger]
  }

  case class SetSocket(socket: Socket)

  val TrackMessage = 10
  val PlayStateMessage = 20
  val AudioBufferMessage = 33 
  val AudioDoneMessage = 40 

}

import StationMessenger._

class StationMessenger extends Actor {

  var socket: Socket = _

  var socketInput: InputStream = _
  var dataInput: DataInputStream = _
  var socketOutput: OutputStream = _
  var dataOutput: DataOutputStream = _

  var synchronizer: Synchronizer = _
  var meanTimeDiff: Int = 0


  def receive = {

    case SetSocket(socket) => 
      socketInput = socket.getInputStream()
      dataInput = new DataInputStream(socketInput)
      socketOutput = socket.getOutputStream()
      dataOutput = new DataOutputStream(socketOutput)

      read()

      synchronizer = new Synchronizer(self, socketOutput, dataOutput, socketInput, dataInput)  
      synchronizer.writeSyncRequest()

      context.become(receiveWriteSync)
  }

  import Synchronizer._

  def receiveWriteSync: Receive = {

    case WriteSyncResult =>
      synchronizer.writeSyncResult()

    case SetMeanTimeDiff(timeDiff) =>
      meanTimeDiff = timeDiff
      Log.d("chakra", "Mean Time Diff: " + meanTimeDiff)

  }

  def read(): Unit = {
    val f = Future {

      import Synchronizer._
      val messageType = dataInput.readInt()
      messageType match {

        case TrackMessage =>
          readTrack()
        case PlayStateMessage =>
          readPlayState()
        case AudioBufferMessage =>
          readAudioBuffer()
        case AudioDoneMessage =>
          mainActorRef ! MainActor.EndStationAudioBuffer

        case SyncResultMessage =>
          synchronizer.readSyncResult()

        case SyncRequestMessage =>
          self ! WriteSyncResult

        case TimeDiffMessage =>
          synchronizer.readTimeDiff()

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

  @throws(classOf[IOException])
  def readTrack(): Unit = {
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
  def readAudioBuffer(): Unit = {

    //read the track path
    val bufferSize = dataInput.readInt()
    val audioBuffer = new Array[Byte](bufferSize)
    socketInput.read(audioBuffer)

    mainActorRef ! MainActor.AddStationAudioBuffer(audioBuffer)

  }

  @throws(classOf[IOException])
  def readPlayState(): Unit = {
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


}




