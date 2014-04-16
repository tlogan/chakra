package com.logan.feelchakra

import android.util.Log

object TrackAudio {
  case class AudioPart(data: Array[Byte])
}

import TrackAudio._

class TrackAudio(
  val track: Track,
  val audio: List[Array[Byte]]
) { 

  def this(track: Track) = this(
    track, List[Array[Byte]]()
  )

  def addAudio(data: Array[Byte]): TrackAudio = {
    val newAudio = audio.:+(data)
    new TrackAudio(track, newAudio)
  }

}
