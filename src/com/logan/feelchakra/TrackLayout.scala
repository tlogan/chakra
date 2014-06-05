package com.logan.feelchakra

import android.util.Log
import android.widget.Toast
import RichListView.listView2RichListView
import RichView.view2RichView
import RichContext.context2RichContext

class TrackLayout(
    context: Context,
    track: Track,
    playmap: Map[Track, List[Int]],
    trackOption: Option[Track] 
) extends SlidingTrackLayout(
  context,
  track,
  new TextLayout(context, track.title, track.artist, track.album) {
    setBackgroundColor(TRANSPARENT)
    setLayoutParams(new LLLayoutParams(MATCH_PARENT, context.dp(64)))
  }
) {

  val color = playmap.get(track) match {
    case Some(posList) => 
      trackOption match {
        case Some(currentTrack) if (currentTrack == track) =>
          BLUE 
        case _ => GRAY 
      }
    case None => DKGRAY 
  }
  slideView.setBackgroundColor(color)

}
