package com.logan.feelchakra

import android.util.Log
import android.widget.Toast
import RichListView.listView2RichListView
import RichView.view2RichView
import RichContext.context2RichContext

trait SlideLayout {
  val trackTextLayout: ViewGroup
  val slideView: View with HorizontalSlideView
  val veiledView: View
}

object SlideLayout {

  def createTrackLayout(
      context: Context,
      track: Track,
      futureTrackMap: Map[Track, Int],
      trackOption: Option[Track] 
  ): View with SlideLayout = { 

    val view = new RelativeLayout(context) with SlideLayout {
      override val trackTextLayout = {
        val t = TextLayout.createTextLayout(context, track.title, track.artist, track.album.title)
        t.setBackgroundColor(TRANSPARENT)
        t
      }
      override val veiledView = SlideLayout.createVeiledView(context)
      override val slideView = HorizontalSlideView.createTrackSlideView(context, track, () => veiledView.getWidth())

    }
    SlideLayout.construct(context, view, track)

    val color = futureTrackMap.get(track) match {
      case Some(pos) => GRAY 
      case None => 
        trackOption match {
          case Some(currentTrack) if (currentTrack == track) => BLUE 
          case _ =>  DKGRAY 
        }
    }

    view.slideView.setBackgroundColor(color)
    view

  }

  def createAlbumTrackLayout(
      context: Context, 
      track: Track,
      futureTrackPosOp: Option[Int],
      textLayout: LinearLayout
  ): ViewGroup with SlideLayout = {
    val view = new RelativeLayout(context) with SlideLayout {
      override val trackTextLayout = textLayout
      override val veiledView = SlideLayout.createVeiledView(context)
      override val slideView = HorizontalSlideView.createTrackSlideView(context, track, () => veiledView.getWidth())
    }

    SlideLayout.construct(context, view, track)
    futureTrackPosOp match {
      case Some(pos) =>
        view.trackTextLayout.addView {
          val tv = new TextView(context)
          tv.setText((pos + 1).toString)
          tv.setTextSize(context.sp(8))
          tv.setTextColor(LTGRAY)
          tv
        }
        view.slideView.setBackgroundColor(GRAY) 
      case None =>
        view.slideView.setBackgroundColor(LDKGRAY) 
    }
    view

  }

  def createVeiledView(context: Context): View = {
    val view = new View(context)
    view.setBackgroundColor(BLUE)
    view.setLayoutParams(new RLLayoutParams(MATCH_PARENT, MATCH_PARENT))
    view
  }


  def construct(context: Context, slideLayout: ViewGroup with SlideLayout, track: Track): Unit = {

    val gesture = new SimpleOnGestureListener {

      val rightGesture = MainGesture.createRightGesture(slideLayout.slideView)

      override def onDown(e: MotionEvent): Boolean = {
        rightGesture.onDown(e)
      }

      override def onScroll(e1: MotionEvent, e2: MotionEvent, distX: Float, distY: Float): Boolean = {
        rightGesture.onScroll(e1, e2, distX, distY)
      }

      override def onFling(e1: MotionEvent, e2: MotionEvent, velX: Float, velY: Float): Boolean = {
        rightGesture.onFling(e1, e2, velX, velY)
      }

      override def onSingleTapUp(e: MotionEvent): Boolean = {
        mainActorRef ! MainActor.AppendOrRemoveFutureTrack(track)
        true
      }
    }

    val gestureDetector = new GestureDetector(context, gesture) 

    slideLayout.setOnTouch((view, event) => {

      if (!gestureDetector.onTouchEvent(event)) {
        event.getAction() match {
          case ACTION_UP => 
            HorizontalSlideView.slide(slideLayout.slideView)
            true
          case ACTION_CANCEL => 
            HorizontalSlideView.slide(slideLayout.slideView)
            true
          case _ =>
            false
        }
      } else true 

    })

    slideLayout.addView(slideLayout.trackTextLayout)
    slideLayout.addView(slideLayout.veiledView)
    slideLayout.addView(slideLayout.slideView)
    slideLayout.bringChildToFront(slideLayout.trackTextLayout)

  }

}
