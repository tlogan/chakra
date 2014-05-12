package com.logan.feelchakra

import android.util.Log

case class LocalManager(
  currentIndex: Int, 
  playlist: List[Track], 
  trackList: List[Track],
  playerOpen: Boolean,
  playing: Boolean, 
  startPos: Int
) {

  def this() = this(-1, List(), List(), false, false, 0)

  import MainActor._
  import UI._

  def optionByIndex(index: Int): Option[Track] = playlist.lift(index)
  def currentOp: Option[Track] = playlist.lift(currentIndex)
  def nextOp: Option[Track] = playlist.lift(currentIndex + 1)


  val artistMap = trackList.foldLeft(new ArtistMap())((artistMap, track) => {

    val albumMap = artistMap.get(track.artist) match {
      case Some(albumMap) =>
        val trackList = albumMap.get(track.album) match {
          case Some(trackList) => trackList :+ track
          case None => List[Track](track)
        }
        albumMap + (track.album -> trackList)
      case None =>
        new AlbumMap() + (track.album -> List[Track](track))
    }

    artistMap + (track.artist -> albumMap)
    
  })

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
    copy(trackList = list)
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
