package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global
 
object Playmap {

  def apply(playlist: List[Track]): Map[Track, TreeSet[Int]] = {
    playlist
      .toIterator
      .zipWithIndex
      .foldLeft(new HashMap[Track, TreeSet[Int]]())((playmap, trackPair) => {
        val track = trackPair._1
        val playlistPos = trackPair._2 + 1

        val posSet = playmap.get(track) match {
          case Some(posSet) => posSet + playlistPos 
          case None => TreeSet[Int](playlistPos)
        }

        playmap + (track -> posSet)

      })
  }

}
