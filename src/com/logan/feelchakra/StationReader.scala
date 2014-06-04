package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global

object StationReader {

  val TrackMessage = 10
  val PlayStateMessage = 20
  val TrackPathMessage = 27 
  val AudioBufferMessage = 33 
  val AudioDoneMessage = 40 

}

import StationReader._
import SocketReader._

class StationReader(socket: Socket, writer: ActorRef) 
  extends SocketReader(socket, writer) 
  with SyncServerReader 
{

  override def receive = receiveReadMessages orElse receiveReadSyncResponse

  val receiveReadMessages: PartialFunction[Any, Unit] = {

    case TrackMessage =>
      Log.d("chakra", "reading track")
      readTrack()
    case TrackPathMessage =>
      Log.d("chakra", "reading current track path")
      readTrackPath()
    case PlayStateMessage =>
      Log.d("chakra", "reading playstate")
      readPlayState()
    case AudioBufferMessage =>
      readAudioBuffer()
    case AudioDoneMessage =>
      readAudioBufferDone()

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

    mainActorRef ! MainActor.AddStationTrack(track)

  }

  @throws(classOf[IOException])
  def readAudioBuffer(): Unit = {

    //read the track path
    val trackPathSize = dataInput.readInt()
    val trackPathBuffer = new Array[Byte](trackPathSize)
    socketInput.read(trackPathBuffer)
    val path = new String(trackPathBuffer, 0, trackPathSize)

    //read the audio 
    val bufferSize = dataInput.readInt()
    val audioBuffer = new Array[Byte](bufferSize)

    var bytesRemaining = bufferSize
    while (bytesRemaining > 0) {
      val bytesRead = socketInput.read(audioBuffer, 0, math.min(bufferSize, bytesRemaining))
      if (bytesRead != -1) {
        mainActorRef ! MainActor.AddStationAudioBuffer(path, audioBuffer.slice(0, bytesRead))
        bytesRemaining = bytesRemaining - bytesRead
      } else {
        bytesRemaining = 0 
      }
    }


  }

  @throws(classOf[IOException])
  def readAudioBufferDone(): Unit = {

    Log.d("chakra", "reading audio done")

    //read the track path
    val trackPathSize = dataInput.readInt()
    val trackPathBuffer = new Array[Byte](trackPathSize)
    socketInput.read(trackPathBuffer)
    val path = new String(trackPathBuffer, 0, trackPathSize)
    mainActorRef ! MainActor.EndStationAudioBuffer(path)

  }

  @throws(classOf[IOException])
  def readTrackPath(): Unit = {
    Log.d("chakra", "reading track ")

    //read the track path
    val trackPathSize = dataInput.readInt()
    val trackPathBuffer = new Array[Byte](trackPathSize)
    socketInput.read(trackPathBuffer)
    val path = new String(trackPathBuffer, 0, trackPathSize)
    mainActorRef ! MainActor.ChangeStationTrackByOriginPath(path)

  }

  @throws(classOf[IOException])
  def readPlayState(): Unit = {
    Log.d("chakra", "reading playState ")
    val foreignStartTime = dataInput.readLong()
    val playState = if (foreignStartTime > 0) {
      val localStartTime = foreignStartTime + timeDiff

      Log.d("chakra", "reading playState with timeDiff: " + timeDiff)
      Playing(localStartTime)
    } else {
      NotPlaying
    }

    mainActorRef ! MainActor.SetStationPlayState(playState)

  }
}
