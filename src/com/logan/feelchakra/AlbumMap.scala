package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global
 
object AlbumMap {

  def apply(trackList: List[Track]): AlbumMap = {
    trackList.foldLeft(new AlbumMap())((albumMap, track) => {
      val trackList = albumMap.get(track.artist) match {
        case Some(trackList) => trackList :+ track
        case None => List[Track](track)
      }
      albumMap + (track.album -> trackList)
    })
  }

  def apply(pair: (String, List[Track])): AlbumMap = {
    TreeMap[String, List[Track]](pair)
  }

}
