package com.logan.feelchakra

import android.util.Log
import android.widget.Toast
import RichListView.listView2RichListView
import RichView.view2RichView

class SlideView(context: Context, computeFinalX: => Int, onSlideEnd: () => Unit) extends View(context) {

  val velMs = 1 
  lazy val finalX = computeFinalX 

  def slideForward(): Unit = {

    Log.d("chakra", "final X: " + finalX)
    Log.d("chakra", "getX: " + getX())
    animate()
      .x(finalX)
      .setDuration((finalX - getX().toInt)/velMs)
      .setListener(new AnimatorListenerAdapter() {
        override def onAnimationEnd(animator: Animator): Unit = {
          onSlideEnd()
        }
      })
  }

  def slideBack(): Unit = {
    animate().x(0).setDuration(getX().toInt/velMs)
  }

  def slide(): Unit = {
    if (getX() > finalX * 2/3) {
      slideForward()
    } else {
      slideBack()
    }
  }


}
