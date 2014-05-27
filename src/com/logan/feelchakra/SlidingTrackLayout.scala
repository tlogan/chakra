package com.logan.feelchakra

import android.util.Log
import android.widget.Toast
import RichListView.listView2RichListView
import RichView.view2RichView

class SlidingTrackLayout(
    context: Context, 
    track: Track,
    val trackTextLayout: ViewGroup 
) extends RelativeLayout(context) {

  lazy val height = 100 

  val slideView = new View(context) with HorizontalSlideView {
    override val velMs = 2
    override val left = 0
    override lazy val right = trackTextLayout.getWidth() 
    override def onSlideLeftEnd() = {} 
    override def onSlideRightEnd() = mainActorRef !  MainActor.AddAndPlayTrack(track)

    setBackgroundColor(DKGRAY)
    setLayoutParams(new RLLayoutParams(MATCH_PARENT, height))
  }

  val veiledView = new View(context) {
    setBackgroundColor(BLUE)
    setLayoutParams(new RLLayoutParams(MATCH_PARENT, height))
  }

  val gestureDetector = new GestureDetector(context, new HorizontalSlideListener(slideView) {

    override def onSingleTapUp(e: MotionEvent): Boolean = {
      mainActorRef ! MainActor.AddPlaylistTrack(track)
      true
    }

  })

  this.setOnTouch((view, event) => {

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

  addView(trackTextLayout)
  addView(veiledView)
  addView(slideView)
  bringChildToFront(trackTextLayout)

}
