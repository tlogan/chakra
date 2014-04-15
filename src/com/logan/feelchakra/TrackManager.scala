package com.logan.feelchakra

import android.util.Log

class TrackManager(
  val currentIndex: Int, 
  val playlist: List[Track], 
  val list: List[Track],
  val playerOpen: Boolean
) {

  def this() = this(-1, List(), List(), false)

  import MainActor._
  import UI._

  def optionByIndex(index: Int): Option[Track] = playlist.lift(index)
  def currentOp: Option[Track] = playlist.lift(currentIndex)
  def nextOp: Option[Track] = playlist.lift(currentIndex + 1)

  def setCurrentIndex(index: Int): TrackManager = {
    mainActorRef ! NotifyHandlers(OnTrackIndexChanged(index))
    mainActorRef ! NotifyHandlers(OnTrackOptionChanged(optionByIndex(index)))
    new TrackManager(index, playlist, list, playerOpen)
  }

  def addPlaylistTrack(track: Track): TrackManager = {
    val newPlaylist = playlist.:+(track)
    mainActorRef ! NotifyHandlers(OnPlaylistChanged(newPlaylist))
    new TrackManager(currentIndex, newPlaylist, list, playerOpen)
  }

  def setList(list: List[Track]): TrackManager = {
    mainActorRef ! NotifyHandlers(OnTrackListChanged(list))
    new TrackManager(currentIndex, playlist, list, playerOpen)
  }

  def setPlayerOpen(playerOpen: Boolean): TrackManager = {
    mainActorRef ! NotifyHandlers(OnPlayerOpenChanged(playerOpen))
    new TrackManager(currentIndex, playlist, list, playerOpen)
  }

  def flipPlayer(): TrackManager = {
    setPlayerOpen(!playerOpen)
  }


  def currentIsLast: Boolean = {
    currentIndex == playlist.size - 1
  }


}
