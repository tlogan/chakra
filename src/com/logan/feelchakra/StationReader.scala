package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global
import akka.pattern.ask
import akka.util.Timeout

object StationReader {

  val PlayStateMessage = 20
  val TrackPathMessage = 27 
  val AudioBufferMessage = 33 
  val AudioDoneMessage = 40 

  def create(socket: Socket, writerRef: ActorRef): Runnable = {

    var timeDiff: Int = 0

    val socketInput: InputStream = socket.getInputStream()
    val dataInput: DataInputStream = new DataInputStream(socketInput)

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
      val playingInt = dataInput.readInt()
      val playState = if (playingInt == 1) {
        val startPos = dataInput.readInt()
        val foreignStartTime = dataInput.readLong()
        val localStartTime = foreignStartTime + timeDiff

        Log.d("chakra", "reading playState with timeDiff: " + timeDiff)
        Playing(startPos, localStartTime)
      } else {
        NotPlaying
      }

      mainActorRef ! MainActor.SetStationPlayState(playState)

    }

    val receive: PartialFunction[Any, Unit] = {

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
    
    new Runnable() {
      override def run(): Unit = Reader.read( 
          socketInput, 
          dataInput,
          receive orElse Reader.receiveReadSyncResponse(
            socketInput, 
            dataInput,
            writerRef,
            {newTimeDiff => timeDiff = newTimeDiff}
          )
      )

    }
  }

}
