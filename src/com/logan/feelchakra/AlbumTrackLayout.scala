package com.logan.feelchakra

import android.util.Log
import android.widget.Toast
import RichListView.listView2RichListView
import RichView.view2RichView

class AlbumTrackLayout(
    context: Context, 
    track: Track,
    trackNum: Int,
    posListOp: Option[List[Int]],
    current: Boolean
) extends SlidingTrackLayout(
  context,
  track,
  new LinearLayout(context) {
    setOrientation(VERTICAL)
    setLayoutParams(new RLLayoutParams(MATCH_PARENT, WRAP_CONTENT))
    setPadding(10, 12, 10, 12)
    setBackgroundColor(TRANSPARENT)
    addView {
      new TextView(context) {
        setText(trackNum + ". " + track.title)
        setTextSize(18)
        setTextColor(WHITE)
      }
    }
  }
) {

  posListOp match {
    case Some(posList) =>
      if (current) {
        slideView.setBackgroundColor(BLUE) 
      } else slideView.setBackgroundColor(GRAY) 
      trackTextLayout.addView {
        new TextView(context) {
          setText(posList.mkString(", "))
          setTextSize(14)
          setTextColor(LTGRAY)
        }
      }
    case None =>
      slideView.setBackgroundColor(DKGRAY) 
  }

}
