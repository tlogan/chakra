package com.logan.feelchakra

import android.util.Log
import android.widget.Toast
import RichListView.listView2RichListView
import RichView.view2RichView
import RichContext.context2RichContext

class AlbumLayout(
    context: Context, 
    album: String,
    trackList: List[Track],
    playmap: Map[Track, List[Int]],
    trackOption: Option[Track]
) extends TextLayout(
    context, 
    album, 
    trackList.size + " Tracks", 
    "time"
) {

  val that = this

  setBackgroundColor(LDKGRAY)

  addView {
    new View(context) {
      setLayoutParams(new LLLayoutParams(MATCH_PARENT, context.dp(4)))
    }
  }

  this.setOnLongClick(view => {
    trackList.foreach(track => {
      mainActorRef ! MainActor.AddPlaylistTrack(track)
    })
    true 
  })


  trackList.toIterator.zipWithIndex.foreach(pair => {
    val track = pair._1
    val trackNum = pair._2 + 1

    val current = trackOption match {
      case Some(currentTrack) if (currentTrack == track) => true
      case _ => false 
    }

    addView(new AlbumTrackLayout(context, track, trackNum, playmap.get(track), current)) 

    if (trackNum != trackList.size) {
      addView {
        new View(context) {
          setBackgroundColor(LTGRAY)
          val lp = new LLLayoutParams(MATCH_PARENT, 1)
          lp.setMargins(context.dp(8), 0, context.dp(8), 0)
          setLayoutParams(lp)
        }
      }
    }
  })

}
