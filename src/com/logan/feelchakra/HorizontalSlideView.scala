package com.logan.feelchakra

import android.util.Log
import android.widget.Toast
import RichListView.listView2RichListView
import RichView.view2RichView

trait HorizontalSlideView {
  this: View =>

  def velMs: Int
  def left: Int
  def right: Int 
  def onSlideRightEnd(): Unit
  def onSlideLeftEnd(): Unit

}

object HorizontalSlideView {

  def createTrackSlideView(context: Context, track: Track, width: () => Int): View with HorizontalSlideView = {
    val view = new View(context) with HorizontalSlideView {
      override val velMs = 2
      override val left = 0
      override lazy val right = width()
      override def onSlideLeftEnd() = {} 
      override def onSlideRightEnd() = mainActorRef !  MainActor.SetPresentTrack(track)
    }
    view.setBackgroundColor(DKGRAY)
    view.setLayoutParams(new RLLayoutParams(MATCH_PARENT, MATCH_PARENT))
    view
  }

  def slideRight(view: View with HorizontalSlideView): Unit = {
    slideRight(view, view.right)
  }

  def slideLeft(view: View with HorizontalSlideView): Unit = {
    slideLeft(view, view.left)
  }

  def slideRight(view: View with HorizontalSlideView, right: Int): Unit = {
    view.animate()
      .x(right)
      .setDuration(Math.abs(right - view.getX().toInt)/view.velMs)
      .setListener(new AnimatorListenerAdapter() {
        override def onAnimationEnd(animator: Animator): Unit = {
          view.onSlideRightEnd()
        }
      })
  }

  def slideLeft(view: View with HorizontalSlideView, left: Int): Unit = {
    view.animate()
      .x(left)
      .setDuration(Math.abs(view.getX().toInt - left)/view.velMs)
      .setListener(new AnimatorListenerAdapter() {
        override def onAnimationEnd(animator: Animator): Unit = {
          view.onSlideLeftEnd()
        }
      })
  }

  def slide(view: View with HorizontalSlideView): Unit = {
    if (view.getX() > view.right / 2) {
      slideRight(view)
    } else {
      slideLeft(view)
    }
  }


}


