package com.logan.feelchakra

import android.util.Log

case class LocalManager(
  pastTrackList: List[Track],
  presentTrackOp: Option[Track],
  futureTrackList: List[Track],
  artistTupleOp: Option[(String, AlbumMap)],
  artistMap: ArtistMap,
  albumTupleOp: Option[(Album, List[Track])],
  albumMap: AlbumMap,
  trackList: List[Track],
  playerOpen: Boolean,
  playing: Boolean, 
  startPos: Int
) {

  def this() = this(List(), None, List(), None, new ArtistMap(), None, AlbumMap(), List(), false, false, 0)

  import MainActor._
  import UI._

  def setPresentTrackFromPastIndex(index: Int): LocalManager = {
    val newPastTrackList = pastTrackList.take(index)
    val newPresentTrackOp = pastTrackList.lift(index)
    val newFutureTrackList = pastTrackList.drop(index + 1) ++ presentTrackOp.toList ++ futureTrackList

    mainActorRef ! NotifyHandlers(OnPastTrackListChanged(newPastTrackList))
    mainActorRef ! NotifyHandlers(OnPresentTrackOptionChanged(newPresentTrackOp))
    mainActorRef ! NotifyHandlers(OnFutureTrackListChanged(newFutureTrackList))

    copy(pastTrackList = newPastTrackList, presentTrackOp = newPresentTrackOp, futureTrackList = newFutureTrackList)
  }

  def setPresentTrackFromFutureIndex(index: Int): LocalManager = {
    val newPastTrackList = pastTrackList ++ presentTrackOp.toList ++ futureTrackList.take(index)
    val newPresentTrackOp = futureTrackList.lift(index)
    val newFutureTrackList = futureTrackList.drop(index + 1)

    mainActorRef ! NotifyHandlers(OnPastTrackListChanged(newPastTrackList))
    mainActorRef ! NotifyHandlers(OnPresentTrackOptionChanged(newPresentTrackOp))
    mainActorRef ! NotifyHandlers(OnFutureTrackListChanged(newFutureTrackList))

    copy(pastTrackList = newPastTrackList, presentTrackOp = newPresentTrackOp, futureTrackList = newFutureTrackList)
  }

  def setPresentTrack(track: Track): LocalManager = {
    val newPastTrackList = pastTrackList ++ presentTrackOp.toList 
    val newPresentTrackOp = Some(track) 

    mainActorRef ! NotifyHandlers(OnPastTrackListChanged(newPastTrackList))
    mainActorRef ! NotifyHandlers(OnPresentTrackOptionChanged(newPresentTrackOp))

    copy(pastTrackList = newPastTrackList, presentTrackOp = newPresentTrackOp)
  }

  def removeFutureTrack(track: Track): LocalManager = {
    val newFutureTrackList = futureTrackList.filter(futureTrack => {
      futureTrack != track
    })
    mainActorRef ! NotifyHandlers(OnFutureTrackListChanged(newFutureTrackList))
    copy(futureTrackList = newFutureTrackList)
  }

  def appendFutureTrack(track: Track): LocalManager = {
    val newFutureTrackList = futureTrackList.:+(track)
    mainActorRef ! NotifyHandlers(OnFutureTrackListChanged(newFutureTrackList))
    copy(futureTrackList = newFutureTrackList)
  }

  def prependFutureTrack(track: Track): LocalManager = {
    val newFutureTrackList = futureTrackList.+:(track)
    mainActorRef ! NotifyHandlers(OnFutureTrackListChanged(newFutureTrackList))
    copy(futureTrackList = newFutureTrackList)
  }

  def setTrackList(list: List[Track]): LocalManager = {

    mainActorRef ! NotifyHandlers(OnTrackListChanged(list))
    val albumMap = AlbumMap(list)
    mainActorRef ! NotifyHandlers(OnAlbumMapChanged(albumMap))
    val artistMap = ArtistMap(list)
    mainActorRef ! NotifyHandlers(OnArtistMapChanged(artistMap))

    copy(trackList = list, albumMap = albumMap, artistMap = artistMap)
  }

  def setArtistTupleOp(artistTupleOp: Option[(String, AlbumMap)]): LocalManager = {
    mainActorRef ! NotifyHandlers(OnArtistTupleOpChanged(artistTupleOp))
    copy(artistTupleOp = artistTupleOp)
  }

  def setAlbumTupleOp(albumTupleOp: Option[(Album, List[Track])]): LocalManager = {
    mainActorRef ! NotifyHandlers(OnAlbumTupleOpChanged(albumTupleOp))
    copy(albumTupleOp = albumTupleOp)
  }

  def setPlayerOpen(playerOpen: Boolean): LocalManager = {
    mainActorRef ! NotifyHandlers(OnPlayerOpenChanged(playerOpen))
    copy(playerOpen = playerOpen)
  }

  def setPlaying(playing: Boolean): LocalManager = {
    mainActorRef ! NotifyHandlers(OnLocalPlayingChanged(playing))
    copy(playing = playing)
  }

  def setStartPos(startPos: Int): LocalManager = {
    mainActorRef ! NotifyHandlers(OnLocalStartPosChanged(startPos))
    copy(startPos = startPos)
  }

  def flipPlayer(): LocalManager = {
    setPlayerOpen(!playerOpen)
  }

}
