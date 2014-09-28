package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global

object TrackDeck {

  def props(): Props = Props[TrackDeck]

  case class Subscribe(ui: Handler)
  case class SetPresentTrack(track: Track)
  case class RemoveFutureTrack(track: Track)
  case class SetPresentTrackToPastIndex(index: Int)
  case class SetPresentTrackToFutureIndex(index: Int)
  case object SetPresentTrackToPrev
  case object SetPresentTrackToNext
  case class PrependFutureTrack(track: Track)
  case class AppendFutureTrack(track: Track)
  case class AppendOrRemoveFutureTrack(track: Track)
  case object WritePresentTrackToListeners

}

class TrackDeck extends Actor {

  import TrackDeck._
  import UI._

  private def update(pastTrackList: List[Track], presentTrackOp: Option[Track], futureTrackList: List[Track]) = {
    context.become(receiveTracks(pastTrackList, presentTrackOp, futureTrackList))
  }

  def receiveTracks(pastTrackList: List[Track], presentTrackOp: Option[Track], futureTrackList: List[Track]): Receive = {

    case Subscribe(ui) =>
      mainActorRef ! MainActor.NotifyHandlers(OnPastTrackListChanged(pastTrackList))
      mainActorRef ! MainActor.NotifyHandlers(OnPresentTrackOptionChanged(presentTrackOp))
      mainActorRef ! MainActor.NotifyHandlers(OnFutureTrackListChanged(futureTrackList))


    case SetPresentTrack(track) =>
      val newPastTrackList = pastTrackList ++ presentTrackOp.toList 
      val newPresentTrackOp = Some(track) 
      update(newPastTrackList, newPresentTrackOp, futureTrackList)
      mainActorRef ! MainActor.NotifyHandlers(OnPastTrackListChanged(newPastTrackList))
      mainActorRef ! MainActor.NotifyHandlers(OnPresentTrackOptionChanged(newPresentTrackOp))


    case RemoveFutureTrack(track) =>
      val newFutureTrackList = futureTrackList.filter(futureTrack => {
        futureTrack != track
      })
      update(pastTrackList, presentTrackOp, newFutureTrackList)
      mainActorRef ! MainActor.NotifyHandlers(OnFutureTrackListChanged(newFutureTrackList))

    case SetPresentTrackToPrev =>
      pastTrackList.lastOption match {
        case Some(track) =>
          self ! SetPresentTrackToPastIndex(pastTrackList.size - 1)
          mainActorRef ! MainActor.PlayTrackIfLocal(track)
        case None =>
      }

    case SetPresentTrackToNext =>
      futureTrackList.headOption match {
        case Some(track) => 
          self ! SetPresentTrackToFutureIndex(0)
          mainActorRef ! MainActor.PlayTrackIfLocal(track)
        case None =>
      }

    case SetPresentTrackToPastIndex(index) =>
      val newPastTrackList = pastTrackList.take(index)
      val newPresentTrackOp = pastTrackList.lift(index)
      val newFutureTrackList = pastTrackList.drop(index + 1) ++ presentTrackOp.toList ++ futureTrackList

      update(newPastTrackList, newPresentTrackOp, newFutureTrackList)

      newPresentTrackOp match {
        case Some(track) => mainActorRef ! MainActor.PlayTrackIfLocal(track)
        case None =>
      }

      mainActorRef ! MainActor.NotifyHandlers(OnPastTrackListChanged(newPastTrackList))
      mainActorRef ! MainActor.NotifyHandlers(OnPresentTrackOptionChanged(newPresentTrackOp))
      mainActorRef ! MainActor.NotifyHandlers(OnFutureTrackListChanged(newFutureTrackList))


    case SetPresentTrackToFutureIndex(index) =>
      val newPastTrackList = pastTrackList ++ presentTrackOp.toList ++ futureTrackList.take(index)
      val newPresentTrackOp = futureTrackList.lift(index)
      val newFutureTrackList = futureTrackList.drop(index + 1)

      update(newPastTrackList, newPresentTrackOp, newFutureTrackList)
      newPresentTrackOp match {
        case Some(track) => mainActorRef ! MainActor.PlayTrackIfLocal(track)
        case None =>
      }
      mainActorRef ! MainActor.NotifyHandlers(OnPastTrackListChanged(newPastTrackList))
      mainActorRef ! MainActor.NotifyHandlers(OnPresentTrackOptionChanged(newPresentTrackOp))
      mainActorRef ! MainActor.NotifyHandlers(OnFutureTrackListChanged(newFutureTrackList))



    case AppendFutureTrack(track) =>
      val newFutureTrackList = futureTrackList.:+(track)
      update(pastTrackList, presentTrackOp, newFutureTrackList)
      mainActorRef ! MainActor.NotifyHandlers(OnFutureTrackListChanged(newFutureTrackList))

    case PrependFutureTrack(track) =>
      val newFutureTrackList = futureTrackList.+:(track)
      update(pastTrackList, presentTrackOp, newFutureTrackList)
      mainActorRef ! MainActor.NotifyHandlers(OnFutureTrackListChanged(newFutureTrackList))

    case AppendOrRemoveFutureTrack(track) =>
      if (futureTrackList.contains(track)) {
        self ! RemoveFutureTrack(track)
      } else {
        self ! PrependFutureTrack(track)
      }
      
    case WritePresentTrackToListeners =>
      presentTrackOp match {
        case Some(track) => mainActorRef ! MainActor.WriteTrackToListeners(track)
        case None =>
      }


  }

  val receive = receiveTracks(List(), None, List())

}
