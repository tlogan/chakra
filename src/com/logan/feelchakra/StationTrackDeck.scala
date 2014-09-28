package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global

object StationTrackDeck {

  def props(): Props = Props[StationTrackDeck]

  case class Subscribe(ui: Handler)
  case class SetTrackOriginPathOp(trackOriginPathOp: Option[String])
  case class CommitTrackTransfer(originPath: String)
  case class AddAudioBuffer(path: String, audioBuffer: Array[Byte], cacheDir: File)

}

class StationTrackDeck extends Actor {

  import StationTrackDeck._
  import UI._

  private def update(
    trackOriginPathOp: Option[String],
    transferredTrackMap: Map[String, Track],
    transferringAudioMap: Map[String, (String, OutputStream)]
  ) = {
    context.become(receiveTracks(
      trackOriginPathOp,
      transferredTrackMap,
      transferringAudioMap
    ))
  }


  def receiveTracks(
    trackOriginPathOp: Option[String],
    transferredTrackMap: Map[String, Track],
    transferringAudioMap: Map[String, (String, OutputStream)]
  ): Receive = {

    case Subscribe(ui: Handler) =>

      val trackOp = trackOriginPathOp match {
        case Some(path) => transferredTrackMap.get(path)
        case None => None
      }

      List(
        OnStationTrackOpChanged(trackOp)
      ).foreach(m => notifyHandler(ui, m))

    case SetTrackOriginPathOp(trackOriginPathOp) =>

      trackOriginPathOp match {
        case Some(trackOriginPath) =>
          transferredTrackMap.get(trackOriginPath) match {
            case Some(track) => mainActorRef ! MainActor.NotifyHandlers(OnStationTrackOpChanged(Some(track)))
            case None => mainActorRef ! MainActor.NotifyHandlers(OnStationTrackOpChanged(None))
          }
        case None => mainActorRef ! MainActor.NotifyHandlers(OnStationTrackOpChanged(None))
      }
      update(trackOriginPathOp, transferredTrackMap, transferringAudioMap)
    

    case CommitTrackTransfer(originPath) =>
      val transferringAudio = transferringAudioMap(originPath)
      transferringAudio._2.close()
      val path = transferringAudio._1

      TrackFuture(path) onComplete {
        case Success(track) => 
          Log.d("chakra", "end station audio buffer track: " + track)
          trackOriginPathOp match {
            case Some(trackOriginPath) if trackOriginPath == originPath =>
              mainActorRef ! MainActor.NotifyHandlers(OnStationTrackOpChanged(Some(track)))
            case _ => 
          }
          update(trackOriginPathOp, transferredTrackMap.+(originPath -> track), transferringAudioMap.-(originPath))
        case Failure(t) => 
          assert(false) 
      }

    

    case AddAudioBuffer(originPath, audioBuffer, cacheDir) =>

      transferringAudioMap.get(originPath) match {
        case None =>
          Log.d("chakra", "AddStationAudioBuffer: " + originPath)
          val name = "chakra" + Platform.currentTime 
          val file = java.io.File.createTempFile(name, null, cacheDir)
          val fileOutput = new BufferedOutputStream(new FileOutputStream(file))
          update(trackOriginPathOp, transferredTrackMap, transferringAudioMap.+(originPath -> (file.getAbsolutePath() -> fileOutput)))
          fileOutput.write(audioBuffer)
        case Some(transferringAudio) =>
          transferringAudio._2.write(audioBuffer)
      }


  }

  val receive = receiveTracks(None, HashMap[String, Track](), HashMap[String, (String, OutputStream)]())


}
