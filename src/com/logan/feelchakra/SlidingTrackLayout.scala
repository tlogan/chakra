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

  val slideView = new View(context) {
    setBackgroundColor(DKGRAY)
    setLayoutParams(new RLLayoutParams(MATCH_PARENT, height))
  }
  val veiledView = new View(context) {
    setBackgroundColor(BLUE)
    setLayoutParams(new RLLayoutParams(MATCH_PARENT, height))
  }

  addView(trackTextLayout)
  addView(veiledView)
  addView(slideView)
  bringChildToFront(trackTextLayout)


  var startX = 0 
  var endX = 0 
  var diffX = 0
  val velMs = 1 

  lazy val finalX = trackTextLayout.getWidth() 

  val gestureDetector = new GestureDetector(context, new SimpleOnGestureListener {
    override def onDown(e: MotionEvent): Boolean = {
      val startX = e.getX().toInt
      Log.d("chakra", "onDown " + startX)
      true
    }

    override def onScroll(e1: MotionEvent, e2: MotionEvent, distX: Float, distY: Float): Boolean = {
      startX = e1.getX().toInt
      endX = e2.getX().toInt
      diffX = endX - startX
      if (diffX > 0) {
        slideView.setX(diffX)
      }
      true
    }

    override def onFling(e1: MotionEvent, e2: MotionEvent, velX: Float, velY: Float): Boolean = {
      startX = e1.getX().toInt
      endX = e2.getX().toInt
      diffX = endX - startX

      if (velX > 0) {
        slideView.animate()
          .x(finalX)
          .setDuration((finalX - diffX)/velMs)
          .setListener(new AnimatorListenerAdapter() {
            override def onAnimationEnd(animator: Animator): Unit = {
              mainActorRef !  MainActor.AddAndPlayTrack(track)
            }
          })
      }

      true
    }
  })


  trackTextLayout.setOnTouch((view, event) => {

    if (!gestureDetector.onTouchEvent(event)) {
      event.getAction() match {
        case ACTION_UP => 
          Log.d("chakra", "action up " + diffX)
          if (diffX > finalX * 2/3) {
            slideView.animate()
              .x(finalX)
              .setDuration((finalX - diffX)/velMs)
              .setListener(new AnimatorListenerAdapter() {
                override def onAnimationEnd(animator: Animator): Unit = {
                  mainActorRef !  MainActor.AddAndPlayTrack(track)
                }
              })
          } else {
            slideView.animate()
              .x(0)
              .setDuration(diffX/velMs)
          }
          true
        case ACTION_CANCEL => 
          slideView.setX(0)
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



}
