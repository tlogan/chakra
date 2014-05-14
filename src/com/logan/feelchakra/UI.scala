package com.logan.feelchakra

object UI {

  sealed trait OnChange 
  case class OnSelectionListChanged(selectionList: List[Selection]) extends OnChange
  case class OnPlayerOpenChanged(playerOpen: Boolean) extends OnChange
  case class OnSelectionChanged(selection: Selection) extends OnChange

  case class OnTrackListChanged(trackList: List[Track]) extends OnChange
  case class OnArtistMapChanged(artistMap: ArtistMap) extends OnChange
  case class OnAlbumMapChanged(albumMap: AlbumMap) extends OnChange

  case class OnArtistTupleOpChanged(artistTupleOp: Option[(String, AlbumMap)]) extends OnChange

  case class OnStationOptionChanged(stationOption: Option[Station]) extends OnChange
  case class OnStationListChanged(stationList: List[Station]) extends OnChange

  case class OnPlaylistChanged(playlist: List[Track]) extends OnChange
  case class OnTrackIndexChanged(trackIndex: Int) extends OnChange

  case class OnLocalTrackOptionChanged(trackOption: Option[Track]) extends OnChange
  case class OnLocalPlayingChanged(playing: Boolean) extends OnChange
  case class OnLocalStartPosChanged(startPos: Int) extends OnChange

  case class OnStationTrackOpChanged(trackOp: Option[Track]) extends OnChange
  case class OnStationAudioBufferDone(trackOp: Option[Track]) extends OnChange
  case class OnStationPlayStateChanged(playState: PlayState) extends OnChange

  case class OnProfileChanged(networkProfile: NetworkProfile) extends OnChange

  case class OnDiscoveringChanged(discovering: Boolean) extends OnChange
  case class OnAdvertisingChanged(advertising: Boolean) extends OnChange


}
