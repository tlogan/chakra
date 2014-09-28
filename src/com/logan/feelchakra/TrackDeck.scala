package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global

object TrackDeck {

  def props(): Props = Props[TrackDeck]

  case class Subscribe(ui: Handler)
  case class SetPresentTrack(track: Track)
  case class SetPresentTrackToPastIndex(index: Int)
  case class SetPresentTrackToFutureIndex(index: Int)
  case object SetPresentTrackToPrev
  case object SetPresentTrackToNext
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
      List(
        OnPastTrackListChanged(pastTrackList),
        OnPresentTrackOptionChanged(presentTrackOp),
        OnFutureTrackListChanged(futureTrackList)
      ).foreach(m => notifyHandler(ui, m))


    case SetPresentTrack(track) =>
      val newPastTrackList = pastTrackList ++ presentTrackOp.toList 
      val newPresentTrackOp = Some(track) 
      val newFutureTrackList = futureTrackList.filter(futureTrack => futureTrack != track )
      update(newPastTrackList, newPresentTrackOp, futureTrackList)
      List(
        OnPastTrackListChanged(newPastTrackList),
        OnPresentTrackOptionChanged(newPresentTrackOp),
        OnFutureTrackListChanged(newFutureTrackList)
      ).foreach(m => mainActorRef ! MainActor.NotifyHandlers(m))

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
      List(
        OnPastTrackListChanged(newPastTrackList),
        OnPresentTrackOptionChanged(newPresentTrackOp),
        OnFutureTrackListChanged(newFutureTrackList)
      ).foreach(m => mainActorRef ! MainActor.NotifyHandlers(m))


    case SetPresentTrackToFutureIndex(index) =>
      val newPastTrackList = pastTrackList ++ presentTrackOp.toList ++ futureTrackList.take(index)
      val newPresentTrackOp = futureTrackList.lift(index)
      val newFutureTrackList = futureTrackList.drop(index + 1)

      update(newPastTrackList, newPresentTrackOp, newFutureTrackList)
      newPresentTrackOp match {
        case Some(track) => mainActorRef ! MainActor.PlayTrackIfLocal(track)
        case None =>
      }
      List(
        OnPastTrackListChanged(newPastTrackList),
        OnPresentTrackOptionChanged(newPresentTrackOp),
        OnFutureTrackListChanged(newFutureTrackList)
      ).foreach(m => mainActorRef ! MainActor.NotifyHandlers(m))

    case AppendOrRemoveFutureTrack(track) =>
      val newFutureTrackList = if (futureTrackList.contains(track)) {
        futureTrackList.filter(futureTrack => {
          futureTrack != track
        })
      } else {
        mainActorRef ! MainActor.WriteTrackToListenersIfStationDisconnected(track)
        futureTrackList.:+(track)
      }
      update(pastTrackList, presentTrackOp, newFutureTrackList)
      mainActorRef ! MainActor.NotifyHandlers(OnFutureTrackListChanged(newFutureTrackList))
      
    case WritePresentTrackToListeners =>
      presentTrackOp match {
        case Some(track) => mainActorRef ! MainActor.WriteTrackToListeners(track)
        case None =>
      }


  }

  val receive = receiveTracks(List(), None, List())

}
