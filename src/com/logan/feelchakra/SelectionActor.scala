package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global

object SelectionActor {

  def props(): Props = Props[SelectionActor]

  case class Subscribe(ui: Handler)
  case class SetSelection(selection: Selection)

}

class SelectionActor extends Actor {

  import SelectionActor._

  def update(selection: Selection, selectionList: List[Selection]) = {
    context.become(mkReceive(selection, selectionList))
  }

  def mkReceive(selection: Selection, selectionList: List[Selection]): Receive = {

    case Subscribe(ui) =>
      List(
        UI.OnSelectionListChanged(selectionList),
        UI.OnSelectionChanged(selection)
      ).foreach(m => notifyHandler(ui, m))

    case SetSelection(_selection) =>
      mainActorRef ! MainActor.NotifyHandlers(UI.OnSelectionChanged(_selection))
      Log.d("chakra", "changing selection " + _selection)
      update(_selection, selectionList)

  }

  val receive = mkReceive(
      ArtistSelection,
      List(StationSelection, ArtistSelection, AlbumSelection, TrackSelection)
  )

}
