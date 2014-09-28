package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global

object TrackLibrary {

  def props(): Props = Props[TrackLibrary]

  case class Subscribe(ui: Handler)
  case class SetTrackList(trackList: List[Track])
  case class SetArtistTupleOp(artistTupleOp: Option[(String, AlbumMap)])
  case class SetAlbumTupleOp(albumTupleOp: Option[(Album, List[Track])])
  case class SelectArtistTuple(artistTuple: (String, AlbumMap))
  case class SelectAlbumTuple(albumTuple: (Album, List[Track]))

}

class TrackLibrary extends Actor {

  import TrackLibrary._
  import UI._

  private def update(
      trackList: List[Track],
      albumMap: AlbumMap,
      albumTupleOp: Option[(Album, List[Track])],
      artistMap: ArtistMap,
      artistTupleOp: Option[(String, AlbumMap)]
  ) = {
    context.become(receiveTracks(
      trackList,
      albumMap,
      albumTupleOp,
      artistMap,
      artistTupleOp
    ))
  }


  def receiveTracks(
      trackList: List[Track],
      albumMap: AlbumMap,
      albumTupleOp: Option[(Album, List[Track])],
      artistMap: ArtistMap,
      artistTupleOp: Option[(String, AlbumMap)]
  ): Receive = {

    case Subscribe(ui) =>
      List(
        OnTrackListChanged(trackList),
        OnAlbumMapChanged(albumMap),
        OnAlbumTupleOpChanged(albumTupleOp),
        OnArtistMapChanged(artistMap),
        OnArtistTupleOpChanged(artistTupleOp)
      ).foreach(m => mainActorRef ! MainActor.NotifyHandlers(m))

    case SetTrackList(trackList) =>
      val artistMap = ArtistMap(trackList)
      val albumMap = AlbumMap(trackList)

      update(trackList, albumMap, albumTupleOp, artistMap, artistTupleOp)
      List(
        OnArtistMapChanged(artistMap),
        OnAlbumMapChanged(albumMap),
        OnTrackListChanged(trackList)
      ).foreach(m => mainActorRef ! MainActor.NotifyHandlers(m))

    case SetArtistTupleOp(artistTupleOp) =>
      update(trackList, albumMap, albumTupleOp, artistMap, artistTupleOp)
      mainActorRef ! MainActor.NotifyHandlers(OnArtistTupleOpChanged(artistTupleOp))

    case SetAlbumTupleOp(albumTupleOp) =>
      update(trackList, albumMap, albumTupleOp, artistMap, artistTupleOp)
      mainActorRef ! MainActor.NotifyHandlers(OnAlbumTupleOpChanged(albumTupleOp))


    case SelectArtistTuple(artistTuple) =>
      artistTupleOp match {
        case Some(currentArtistTuple) if currentArtistTuple == artistTuple =>
          self ! SetArtistTupleOp(None)
        case _ =>
          self ! SetArtistTupleOp(Some(artistTuple))
      }

        case SelectAlbumTuple(albumTuple) =>
      albumTupleOp match {
        case Some(currentAlbumTuple) if currentAlbumTuple == albumTuple =>
          self ! SetAlbumTupleOp(None)
        case _ =>
          self ! SetAlbumTupleOp(Some(albumTuple))
      }

  }

  val receive = receiveTracks(List(), AlbumMap(), None, new ArtistMap(), None)

}
