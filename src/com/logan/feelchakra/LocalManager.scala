package com.logan.feelchakra

import android.util.Log

case class LocalManager(
  currentIndex: Int, 
  currentDuration: Int,
  playlist: List[Track], 
  artistTupleOp: Option[(String, AlbumMap)],
  artistMap: ArtistMap,
  albumTupleOp: Option[(String, List[Track])],
  albumMap: AlbumMap,
  trackList: List[Track],
  playerOpen: Boolean,
  playing: Boolean, 
  startPos: Int
) {

  def this() = this(-1, -1, List(), None, new ArtistMap(), None, new AlbumMap(), List(), false, false, 0)

  import MainActor._
  import UI._

  def optionByIndex(index: Int): Option[Track] = playlist.lift(index)
  def currentOp: Option[Track] = playlist.lift(currentIndex)
  def prevOp: Option[Track] = playlist.lift(currentIndex - 1)
  def nextOp: Option[Track] = playlist.lift(currentIndex + 1)

  def setCurrentDuration(duration: Int): LocalManager = {
    mainActorRef ! NotifyHandlers(OnTrackDurationChanged(duration))
    copy(currentDuration = duration)
  }

  def setCurrentIndex(index: Int): LocalManager = {
    mainActorRef ! NotifyHandlers(OnTrackIndexChanged(index))
    val trackOption = optionByIndex(index)
    mainActorRef ! NotifyHandlers(OnLocalTrackOptionChanged(trackOption))
    mainActorRef ! NotifyHandlers(OnPrevTrackOptionChanged(optionByIndex(index - 1)))
    mainActorRef ! NotifyHandlers(OnNextTrackOptionChanged(optionByIndex(index + 1)))
    copy(currentIndex = index)
  }
   
  def addPlaylistTrack(track: Track): LocalManager = {
    val newPlaylist = playlist.:+(track)
    mainActorRef ! NotifyHandlers(OnPlaylistChanged(newPlaylist))
    if (currentIndex + 1 == newPlaylist.size - 1) {
      mainActorRef ! NotifyHandlers(OnNextTrackOptionChanged(Some(track)))
    }
    copy(playlist = newPlaylist)
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

  def setAlbumTupleOp(albumTupleOp: Option[(String, List[Track])]): LocalManager = {
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


  def currentIsLast: Boolean = {
    currentIndex == playlist.size - 1
  }

}
