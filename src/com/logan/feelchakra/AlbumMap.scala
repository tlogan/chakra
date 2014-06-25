package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global
 
object AlbumMap {

  implicit val OrderingAlbum = Ordering.by((_: Album).title)

  def apply(trackList: List[Track]): AlbumMap = {
    trackList.foldLeft(new AlbumMap())((albumMap, track) => {
      val trackList = albumMap.get(track.album) match {
        case Some(trackList) => trackList :+ track
        case None => List[Track](track)
      }
      albumMap + (track.album -> trackList)
    })
  }

  def apply(pair: (Album, List[Track])): AlbumMap = {
    TreeMap[Album, List[Track]](pair)
  }

  def apply(): AlbumMap = {
    TreeMap[Album, List[Track]]()
  }

}
