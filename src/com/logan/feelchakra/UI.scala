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
  case class OnAlbumTupleOpChanged(albumTupleOp: Option[(Album, List[Track])]) extends OnChange

  case class OnStationOptionChanged(stationOption: Option[Station]) extends OnChange
  case class OnStationListChanged(stationList: List[Station]) extends OnChange

  case class OnPastTrackListChanged(pastTrackList: List[Track]) extends OnChange
  case class OnPresentTrackOptionChanged(presentTrackOp: Option[Track]) extends OnChange
  case class OnFutureTrackListChanged(futureTrackList: List[Track]) extends OnChange 

  case class OnLocalPlayingChanged(playing: Boolean) extends OnChange
  case class OnLocalStartPosChanged(startPos: Int) extends OnChange

  case class OnStationTrackOpChanged(trackOp: Option[Track]) extends OnChange
  case class OnStationPlayStateChanged(playState: PlayState) extends OnChange

  case class OnProfileChanged(networkProfile: NetworkProfile) extends OnChange

  case class OnDiscoveringChanged(discovering: Boolean) extends OnChange
  case class OnAdvertisingChanged(advertising: Boolean) extends OnChange

}
