package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global
 
object Playmap {

  def apply(playlist: List[Track]): Map[Track, List[Int]] = {
    playlist
      .toIterator
      .zipWithIndex
      .foldLeft(new HashMap[Track, List[Int]]())((playmap, trackPair) => {
        val track = trackPair._1
        val trackNum = trackPair._2 + 1

        val posList = playmap.get(track) match {
          case Some(posList) => posList :+ trackNum
          case None => List[Int](trackNum)
        }

        playmap + (track -> posList)

      })
  }

}
