package com.logan.feelchakra

import android.util.Log

class LocalManager(
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

  def setCurrentIndex(index: Int): LocalManager = {
    mainActorRef ! NotifyHandlers(OnTrackIndexChanged(index))
    mainActorRef ! NotifyHandlers(OnTrackOptionChanged(optionByIndex(index)))
    new LocalManager(index, playlist, list, playerOpen)
  }

  def addPlaylistTrack(track: Track): LocalManager = {
    val newPlaylist = playlist.:+(track)
    mainActorRef ! NotifyHandlers(OnPlaylistChanged(newPlaylist))
    new LocalManager(currentIndex, newPlaylist, list, playerOpen)
  }

  def setList(list: List[Track]): LocalManager = {
    mainActorRef ! NotifyHandlers(OnTrackListChanged(list))
    new LocalManager(currentIndex, playlist, list, playerOpen)
  }

  def setPlayerOpen(playerOpen: Boolean): LocalManager = {
    mainActorRef ! NotifyHandlers(OnPlayerOpenChanged(playerOpen))
    new LocalManager(currentIndex, playlist, list, playerOpen)
  }

  def flipPlayer(): LocalManager = {
    setPlayerOpen(!playerOpen)
  }


  def currentIsLast: Boolean = {
    currentIndex == playlist.size - 1
  }


}
