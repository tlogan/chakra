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
      readTrack()
    case PlayStateMessage =>
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
