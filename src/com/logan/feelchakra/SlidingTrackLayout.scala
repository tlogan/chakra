package com.logan.feelchakra

import android.util.Log
import android.widget.Toast
import RichListView.listView2RichListView
import RichView.view2RichView

class SlidingTrackLayout(
    context: Context, 
    track: Track,
    trackNum: Int,
    posListOp: Option[List[Int]],
    current: Boolean
) extends RelativeLayout(context) {

  val slideView = new View(context) {
    setBackgroundColor(DKGRAY)
    setLayoutParams(new RLLayoutParams(MATCH_PARENT, 80))
  }
  val veiledView = new View(context) {
    setBackgroundColor(BLUE)
    setLayoutParams(new RLLayoutParams(MATCH_PARENT, 80))
  }

  val trackTextLayout = new LinearLayout(context) {
    setOrientation(VERTICAL)
    setLayoutParams(new RLLayoutParams(MATCH_PARENT, WRAP_CONTENT))
    setPadding(10, 12, 10, 12)
    setBackgroundColor(TRANSPARENT)
    this.setOnClick(view => {
      mainActorRef ! MainActor.AddPlaylistTrack(track)
    })
    

    addView {
      new TextView(context) {
        setText(trackNum + ". " + track.title)
        setTextSize(18)
        setTextColor(WHITE)
      }
    }

  }

  var downX = 0
  var upX = 0
  var diffX = 0

  trackTextLayout.setOnTouch((view, event) => {
    event.getAction() match {
      case ACTION_DOWN => 
        downX = event.getX().toInt
        Log.d("chakra", "action down " + downX)
        true
      case ACTION_MOVE => 
        upX = event.getX().toInt
        Log.d("chakra", "action move " + upX)
        diffX = upX - downX
        if (diffX > 0) {
          val lp = new RLLayoutParams(MATCH_PARENT, 80)
          lp.setMargins(diffX, 0, 0, 0)
          slideView.setLayoutParams(lp)
        }
        true
      case ACTION_UP =>
        Log.d("chakra", "action up " + diffX)
        if (diffX > 100) {
          mainActorRef ! MainActor.AddPlaylistTrack(track)
        }
        downX = 0
        upX = 0
        diffX = 0
        val lp = new RLLayoutParams(MATCH_PARENT, 80)
        lp.setMargins(0, 0, 0, 0)
        slideView.setLayoutParams(lp)
        true
      case _ =>
        false
    }
    
  })

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


  addView(trackTextLayout)
  addView(veiledView)
  addView(slideView)
  bringChildToFront(trackTextLayout)

}
