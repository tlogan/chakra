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

  def slideRight(): Unit = {
    slideRight(right)
  }

  def slideLeft(): Unit = {
    slideLeft(left)
  }

  def slideRight(right: Int): Unit = {
    animate()
      .x(right)
      .setDuration(Math.abs(right - getX().toInt)/velMs)
      .setListener(new AnimatorListenerAdapter() {
        override def onAnimationEnd(animator: Animator): Unit = {
          onSlideRightEnd()
        }
      })
  }

  def slideLeft(left: Int): Unit = {
    animate()
      .x(left)
      .setDuration(Math.abs(getX().toInt - left)/velMs)
      .setListener(new AnimatorListenerAdapter() {
        override def onAnimationEnd(animator: Animator): Unit = {
          onSlideLeftEnd()
        }
      })
  }

  def slide(): Unit = {
    if (getX() > right / 2) {
      slideRight()
    } else {
      slideLeft()
    }
  }


}
