package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global

object StationReader {

  val TrackMessage = 10
  val PlayStateMessage = 20
  val AudioBufferMessage = 33 
  val AudioDoneMessage = 40 

}

import StationReader._
import SocketReader._

class StationReader(socket: Socket, writer: ActorRef) extends SocketReader(socket, writer) {

  override def receive = receiveReadMessages orElse receiveReadSync

  val receiveReadMessages: PartialFunction[Any, Unit] = {

    case TrackMessage =>
      Log.d("chakra", "reading track")
      readTrack()
    case PlayStateMessage =>
      Log.d("chakra", "reading playstate")
      readPlayState()
    case AudioBufferMessage =>
      readAudioBuffer()
    case AudioDoneMessage =>
      mainActorRef ! MainActor.EndStationAudioBuffer

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
    socketInput.read(audioBuffer, 0, bufferSize)

    mainActorRef ! MainActor.AddStationAudioBuffer(audioBuffer)
  }

  @throws(classOf[IOException])
  def readPlayState(): Unit = {
    Log.d("chakra", "reading playState ")
    val foreignStartTime = dataInput.readLong()
    val playState = if (foreignStartTime > 0) {
      val localStartTime = foreignStartTime + meanTimeDiff

      Log.d("chakra", "reading playState with meanTimeDiff: " + meanTimeDiff)
      Playing(localStartTime)
    } else {
      NotPlaying
    }

    mainActorRef ! MainActor.SetStationPlayState(playState)

  }
}
