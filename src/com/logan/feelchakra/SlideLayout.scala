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
      futurePosOp: Option[Int],
      current: Boolean 
  ): View with SlideLayout = { 

    val view = new RelativeLayout(context) with SlideLayout {
      override val trackTextLayout = {
        val t = new LinearLayout(context)
        t.setOrientation(VERTICAL)
        t.addView { 
          val ll = new LinearLayout(context)
          ll.setOrientation(HORIZONTAL)
          val mainTextView = TextView.createMajor(context, track.title)
          mainTextView.setLayoutParams(new LLLayoutParams(0, MATCH_PARENT, 20))
          ll.addView(mainTextView)

          futurePosOp match {
            case Some(pos) =>
              ll.addView {
                val tv = new TextView(context)
                tv.setLayoutParams(new LLLayoutParams(0, MATCH_PARENT, 1))
                tv.setText((pos + 1).toString)
                tv.setTextSize(context.sp(10))
                tv.setTextColor(WHITE)
                tv
              }
            case None =>
          }

          ll
        }
        val secondTextView = TextView.createMinor(context, track.artist)
        val thirdTextView = TextView.createMinor(context, track.album.title)
        List(secondTextView, thirdTextView).foreach(textView => {
          t.addView(textView)
        })

        t.setLayoutParams(new LLLayoutParams(0, MATCH_PARENT, 20))
        t.setBackgroundColor(TRANSPARENT)
        t
      }
      override val veiledView = SlideLayout.createVeiledView(context)
      override val slideView = HorizontalSlideView.createTrackSlideView(context, track, () => veiledView.getWidth())

    }
    SlideLayout.construct(context, view, track)

    val color = futurePosOp match {
      case Some(pos) => GRAY 
      case None if (current) => BLUE
      case _ => DKGRAY
    }


    view.slideView.setBackgroundColor(color)
    view

  }

  def createAlbumTrackLayout(
      context: Context, 
      track: Track,
      color: Int,
      textLayout: LinearLayout
  ): ViewGroup with SlideLayout = {
    val view = new RelativeLayout(context) with SlideLayout {
      override val trackTextLayout = textLayout
      override val veiledView = SlideLayout.createVeiledView(context)
      override val slideView = HorizontalSlideView.createTrackSlideView(context, track, () => veiledView.getWidth())
    }
    SlideLayout.construct(context, view, track)
    view.slideView.setBackgroundColor(color) 
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


    slideLayout.trackTextLayout.setLayoutParams(new LLLayoutParams(MATCH_PARENT, MATCH_PARENT))
    slideLayout.addView(slideLayout.trackTextLayout)
    slideLayout.addView(slideLayout.veiledView)
    slideLayout.addView(slideLayout.slideView)
    slideLayout.bringChildToFront(slideLayout.trackTextLayout)

  }

}
