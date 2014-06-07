package com.logan.feelchakra

import android.util.Log
import android.widget.Toast
import RichListView.listView2RichListView
import RichView.view2RichView
import RichContext.context2RichContext

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
    setPadding(context.dp(4), context.dp(6), context.dp(4), context.dp(6))
    setBackgroundColor(TRANSPARENT)
    addView {
      new TextView(context) {
        setText(trackNum + ". " + track.title)
        setTextSize(context.sp(10))
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
          setTextSize(context.sp(8))
          setTextColor(LTGRAY)
        }
      }
    case None =>
      slideView.setBackgroundColor(LDKGRAY) 
  }

}
