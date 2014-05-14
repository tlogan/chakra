package com.logan.feelchakra

import android.util.Log

case class LocalManager(
  currentIndex: Int, 
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

  def this() = this(-1, List(), None, new ArtistMap(), None, new AlbumMap(), List(), false, false, 0)

  import MainActor._
  import UI._

  def optionByIndex(index: Int): Option[Track] = playlist.lift(index)
  def currentOp: Option[Track] = playlist.lift(currentIndex)
  def nextOp: Option[Track] = playlist.lift(currentIndex + 1)

  def setCurrentIndex(index: Int): LocalManager = {
    mainActorRef ! NotifyHandlers(OnTrackIndexChanged(index))

    val trackOption = optionByIndex(index)
    mainActorRef ! NotifyHandlers(OnLocalTrackOptionChanged(trackOption))
    trackOption match {
      case None => {} 
      case Some(track) =>
        AudioReader(track.path).subscribe(
          audioBuffer => { mainActorRef ! AddLocalAudioBuffer(audioBuffer) },
          t => {  },
          () => { mainActorRef ! EndLocalAudioBuffer }
        )
    }


    copy(currentIndex = index)
  }
   
  def addPlaylistTrack(track: Track): LocalManager = {
    val newPlaylist = playlist.:+(track)
    mainActorRef ! NotifyHandlers(OnPlaylistChanged(newPlaylist))
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

  def setArtistTuple(artistTuple: (String, AlbumMap)): LocalManager = {
    val op = Some(artistTuple)
    mainActorRef ! NotifyHandlers(OnArtistTupleOpChanged(op))
    copy(artistTupleOp = op)
  }

  def setAlbumTuple(albumTuple: (String, List[Track])): LocalManager = {
    val op = Some(albumTuple)
    mainActorRef ! NotifyHandlers(OnAlbumTupleOpChanged(op))
    copy(albumTupleOp = op)
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
