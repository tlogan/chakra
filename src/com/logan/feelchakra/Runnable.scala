package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global
import akka.pattern.ask
import akka.util.Timeout

object Runnable {

  val TrackMessage = 10
  val PlayStateMessage = 20
  val TrackPathMessage = 27 
  val AudioBufferMessage = 33 
  val AudioDoneMessage = 40 

  val SyncResponseMessage = 60 

  val SyncRequestMessage = 50 

  def read(socketInput: InputStream, dataInput: DataInputStream, receive: PartialFunction[Any, Unit]): Unit = {
    val f = Future {
      val messageType = dataInput.readInt()
      try {
        receive(messageType)
      } catch {
        case e: Throwable => 
          Log.d("chakra", "error reading message: " + messageType)
          throw e
      }

    } onComplete {
      case Success(_) => read(socketInput, dataInput, receive)
      case Failure(e) => 
        try {
          socketInput.close()
        } catch {
          case e: IOException => 
            Log.d("chakra", "error closing socket")
            e.printStackTrace()
        } finally {
          Log.d("chakra", "error reading")
          throw e
        }
    }

  }

  def receiveReadSyncRequest(writerRef: ActorRef): PartialFunction[Any, Unit] = {
    PartialFunction[Any, Unit] {
      case SyncRequestMessage =>
        writerRef ! ListenerWriter.WriteSyncResponse(Platform.currentTime)
    }
  }

  def receiveReadSyncResponse(
      socketInput: InputStream, 
      dataInput: DataInputStream,
      writerRef: ActorRef,
      onTimeDiff: Int => Unit
  ): PartialFunction[Any, Unit] = {

    implicit val timeout = Timeout(5000)

    def localTimeDiff(
        syncRequestWriteTime: Long,
        syncRequestReadTime: Long,
        syncResponseWriteTime: Long, 
        syncResponseReadTime: Long 
    ): Int = {
      ((
        syncResponseReadTime + 
        syncRequestWriteTime - 
        syncResponseWriteTime - 
        syncRequestReadTime
      )/2).toInt
    }

    @throws(classOf[IOException])
    def readSyncResponse(): Unit = {
      Log.d("chakra", "reading sync result")

      val syncResponseReadTime = Platform.currentTime
      val syncRequestReadTime = dataInput.readLong()
      val syncResponseWriteTime = dataInput.readLong()

      val f = writerRef ? StationWriter.GetSyncRequestWriteTime
      f.onComplete {
        case Success(syncRequestWriteTime: Long) => 
          Log.d("chakra", "t0 " + syncRequestWriteTime)
          Log.d("chakra", "t1 " + syncRequestReadTime)
          Log.d("chakra", "t2 " + syncResponseWriteTime)
          Log.d("chakra", "t3 " + syncResponseReadTime)

          val timeDiff = localTimeDiff(
              syncRequestWriteTime, syncRequestReadTime,
              syncResponseWriteTime, syncResponseReadTime
          )

          onTimeDiff(timeDiff)

          Log.d("chakra", "LocalTimeDiff: " + timeDiff)

        case Failure(e) =>
          Log.d("chakra", "failed asking for localTimeDiff: " + e.getMessage)
      }

    }

    PartialFunction[Any, Unit] {
      case SyncResponseMessage =>
        readSyncResponse()
    }

  }


  def createListenerReader(socket: Socket, writerRef: ActorRef): Runnable = {
    val socketInput: InputStream = socket.getInputStream()
    val dataInput: DataInputStream = new DataInputStream(socketInput)
    new Runnable() {
      override def run(): Unit = Runnable.read( 
          socketInput, 
          dataInput,
          receiveReadSyncRequest(writerRef)
      )
    }
  }


  def createStationReader(socket: Socket, writerRef: ActorRef): Runnable = {

    var timeDiff: Int = 0

    val socketInput: InputStream = socket.getInputStream()
    val dataInput: DataInputStream = new DataInputStream(socketInput)

    @throws(classOf[IOException])
    def readTrack(): Unit = {
      Log.d("chakra", "reading track ")

      //read the track path
      val trackPathSize = dataInput.readInt()
      val trackPathBuffer = new Array[Byte](trackPathSize)
      socketInput.read(trackPathBuffer)
      val path = new String(trackPathBuffer, 0, trackPathSize)
      val track = Track(path, "", Album("", ""), "", 0)

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

    val receive: PartialFunction[Any, Unit] = {

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
    
    new Runnable() {
      override def run(): Unit = Runnable.read( 
          socketInput, 
          dataInput,
          receive orElse receiveReadSyncResponse(
            socketInput, 
            dataInput,
            writerRef,
            {newTimeDiff => timeDiff = newTimeDiff}
          )
      )

    }
  }

}
