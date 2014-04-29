package com.logan.feelchakra

import android.util.Log

import MainActor._
import UI._

class RemoteManager(
  val currentTrackOp: Option[Track],
  nextTrackOp: Option[Track],
  cacheDir: File,
  trackFileOp: Option[File]
) { 

  def this(cacheDir: File) = this(None, None, cacheDir, None)

  private def createTempFile(originalPath: String): File = {
    val prefix = originalPath.split("/").takeRight(1)(0) + Platform.currentTime
    Log.d("chakra", "prefix: " + prefix) 
    java.io.File.createTempFile(prefix, null, cacheDir)
  }

  def setCurrentTrackOp(track: Track): RemoteManager = {
    val newTrackFile = createTempFile(track.path)
    val newTrack = track.copy(path = newTrackFile.getAbsolutePath())

    mainActorRef ! NotifyHandlers(OnRemoteTrackOptionChanged(Some(newTrack)))

    new RemoteManager(Some(newTrack), nextTrackOp, cacheDir, Some(newTrackFile))

  }

  def setNextTrackOp(track: Track): RemoteManager = {
    val newTrackFile = createTempFile(track.path)
    val newTrack = track.copy(path = newTrackFile.getAbsolutePath())

    new RemoteManager(currentTrackOp, Some(newTrack), cacheDir, Some(newTrackFile))
  }

  val fileOutputOp = trackFileOp match {
    case None => None
    case Some(trackFile) =>
      Some(new FileOutputStream(trackFile))
  }

  def addAudio(data: Array[Byte]): RemoteManager = {
    fileOutputOp match {
      case None => Log.d("chakra", "Cannot write to non-existent file") 
      case Some(fileOutput) =>
        fileOutput.write(data)
    }
    this
  }

  def close(): RemoteManager = {
    fileOutputOp match {
      case None => Log.d("chakra", "Cannot write to non-existent file") 
      case Some(fileOutput) =>
        fileOutput.close()
        Log.d("chakra", "Closing") 

    }
    new RemoteManager(currentTrackOp, nextTrackOp, cacheDir, None)
  }

}
