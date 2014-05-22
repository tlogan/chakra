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

  val height = 80 

  val trackTextLayout = new LinearLayout(context) {
    setOrientation(VERTICAL)
    setLayoutParams(new RLLayoutParams(MATCH_PARENT, height))
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

  val slideView = new SlideView(
    context, 
    trackTextLayout.getWidth(),  
    () => mainActorRef !  MainActor.AddAndPlayTrack(track)
  ) {
    setBackgroundColor(DKGRAY)
    setLayoutParams(new RLLayoutParams(MATCH_PARENT, height))
  }

  val veiledView = new View(context) {
    setBackgroundColor(BLUE)
    setLayoutParams(new RLLayoutParams(MATCH_PARENT, height))
  }

  val gestureDetector = new GestureDetector(context, new SimpleOnGestureListener {
    override def onDown(e: MotionEvent): Boolean = {
      true
    }

    override def onScroll(e1: MotionEvent, e2: MotionEvent, distX: Float, distY: Float): Boolean = {
      val newX = e2.getX().toInt - e1.getX().toInt 
      if (newX > 0) {
        slideView.setX(newX)
      }
      true
    }

    override def onFling(e1: MotionEvent, e2: MotionEvent, velX: Float, velY: Float): Boolean = {
      if (velX > 0) {
        slideView.slideForward()
      }
      true
    }
  })

  trackTextLayout.setOnTouch((view, event) => {

    if (!gestureDetector.onTouchEvent(event)) {
      event.getAction() match {
        case ACTION_UP => 
          slideView.slide()
          true
        case ACTION_CANCEL => 
          slideView.slide()
          true
        case _ =>
          false
      }
    } else true 

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
