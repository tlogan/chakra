package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global
 
object Playmap {

  def apply(playlist: List[Track]): Map[Track, Int] = {
    playlist
      .toIterator
      .zipWithIndex
      .foldLeft(new HashMap[Track, Int]())((playmap, trackPair) => {
        val track = trackPair._1
        val playlistPos = trackPair._2 + 1

        playmap + (track -> playlistPos)

      })
  }

}
