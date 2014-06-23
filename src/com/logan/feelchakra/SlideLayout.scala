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
      playmap: Map[Track, List[Int]],
      trackOption: Option[Track] 
  ): View with SlideLayout = { 

    val view = new RelativeLayout(context) with SlideLayout {
      override val trackTextLayout = {
        val t = TextLayout.createTextLayout(context, track.title, track.artist, track.album)
        t.setBackgroundColor(TRANSPARENT)
        t
      }
      override val slideView = SlideLayout.createTrackSlideView(context, track, () => trackTextLayout.getWidth())
      override val veiledView = SlideLayout.createVeiledView(context)

    }
    SlideLayout.construct(context, view, track)

    val color = playmap.get(track) match {
      case Some(posList) => 
        trackOption match {
          case Some(currentTrack) if (currentTrack == track) =>
            BLUE 
          case _ => GRAY 
        }
      case None => DKGRAY 
    }
    view.slideView.setBackgroundColor(color)
    view

  }

  def createAlbumTrackLayout(
      context: Context, 
      track: Track,
      trackNum: Int,
      posListOp: Option[List[Int]],
      current: Boolean
  ): ViewGroup with SlideLayout = {
    val view = new RelativeLayout(context) with SlideLayout {
      override val trackTextLayout = { 
        val l = new LinearLayout(context)
        l.setOrientation(VERTICAL)
        l.setPadding(context.dp(4), context.dp(6), context.dp(4), context.dp(6))
        l.setBackgroundColor(TRANSPARENT)
        l.addView {
          val v = new TextView(context)
          v.setText(trackNum + ". " + track.title)
          v.setTextSize(context.sp(10))
          v.setTextColor(WHITE)
          v
        }
        l
      }
      override val slideView = SlideLayout.createTrackSlideView(context, track, () => trackTextLayout.getWidth())
      override val veiledView = SlideLayout.createVeiledView(context)
    }

    SlideLayout.construct(context, view, track)
    posListOp match {
      case Some(posList) =>
        if (current) {
          view.slideView.setBackgroundColor(BLUE) 
        } else {
          view.slideView.setBackgroundColor(GRAY) 
        }
        view.trackTextLayout.addView {
          val tv = new TextView(context)
          tv.setText(posList.mkString(", "))
          tv.setTextSize(context.sp(8))
          tv.setTextColor(LTGRAY)
          tv
        }
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

  def createTrackSlideView(context: Context, track: Track, width: () => Int): View with HorizontalSlideView = {
    val view = new View(context) with HorizontalSlideView {
      override val velMs = 2
      override val left = 0
      override lazy val right = width()
      override def onSlideLeftEnd() = {} 
      override def onSlideRightEnd() = mainActorRef !  MainActor.AddAndPlayTrack(track)
    }
    view.setBackgroundColor(DKGRAY)
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
        mainActorRef ! MainActor.AddPlaylistTrack(track)
        true
      }
    }

    val gestureDetector = new GestureDetector(context, gesture) 

    slideLayout.setOnTouch((view, event) => {

      if (!gestureDetector.onTouchEvent(event)) {
        event.getAction() match {
          case ACTION_UP => 
            Slider.slide(slideLayout.slideView)
            true
          case ACTION_CANCEL => 
            Slider.slide(slideLayout.slideView)
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
