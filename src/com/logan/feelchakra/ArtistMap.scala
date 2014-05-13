package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global
 
object ArtistMap {

  def apply(trackList: List[Track]): ArtistMap = {

    trackList.foldLeft(new ArtistMap())((artistMap, track) => {
      val albumMap = artistMap.get(track.artist) match {
        case Some(albumMap) =>
          val trackList = albumMap.get(track.album) match {
            case Some(trackList) => trackList :+ track
            case None => List[Track](track)
          }
          albumMap + (track.album -> trackList)
        case None =>
          AlbumMap(track.album -> List[Track](track))
      }

      artistMap + (track.artist -> albumMap)
    })
  }

}
