package com.logan.feelchakra

import android.util.Log

class TrackAudio(
  val track: Track,
  val audio: File 
) { 

  val fileOutput = new FileOutputStream(audio)

  def addAudio(data: Array[Byte]): TrackAudio = {
    fileOutput.write(data)
    this
  }

  def close(): TrackAudio = {
    fileOutput.close()
    this
  }

}
