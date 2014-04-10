package com.logan.feelchakra

object UI {

  sealed trait OnChange 
  case class OnSelectionListChanged(selectionList: List[Selection]) extends OnChange
  case class OnPlayerOpenChanged(playerOpen: Boolean) extends OnChange
  case class OnSelectionChanged(selection: Selection) extends OnChange
  case class OnTrackListChanged(trackList: List[Track]) extends OnChange
  case class OnStationOptionChanged(stationOption: Option[Station]) extends OnChange
  case class OnStationListChanged(stationList: List[Station]) extends OnChange
  case class OnTrackIndexChanged(trackIndex: Int) extends OnChange
  case class OnPlaylistChanged(playlist: List[Track]) extends OnChange

  case class OnTrackOptionChanged(trackOption: Option[Track]) extends OnChange
  case class OnPlayStateChanged(playOncePrepared: Boolean) extends OnChange
  case class OnPositionChanged(positionOncePrepared: Int) extends OnChange
  case class OnProfileChanged(localAddress: InetSocketAddress, serviceName: String, serviceType: String) extends OnChange
  case class OnRemoteTrackChanged(track: Track) extends OnChange

  case class OnDiscoveringChanged(discovering: Boolean) extends OnChange
  case class OnAdvertisingChanged(advertising: Boolean) extends OnChange


}
