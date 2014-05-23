package com.logan.feelchakra

import android.util.Log
import android.widget.Toast
import RichListView.listView2RichListView
import RichView.view2RichView

trait VerticalSlideView {
  this: View =>

  def velMs: Int
  def bottomY: Int
  def topY: Int 
  def onSlideUpEnd(): Unit
  def onSlideDownEnd(): Unit

  def slideUp(): Unit = {
    animate()
      .y(topY)
      .setDuration((getY().toInt - topY)/velMs)
      .setListener(new AnimatorListenerAdapter() {
        override def onAnimationEnd(animator: Animator): Unit = {
          onSlideUpEnd()
        }
      })
  }

  def slideDown(): Unit = {
    animate().y(bottomY).setDuration((bottomY - getY().toInt)/velMs)
      .setListener(new AnimatorListenerAdapter() {
        override def onAnimationEnd(animator: Animator): Unit = {
          onSlideDownEnd()
        }
      })
  }

  def slide(): Unit = {
    if (getY() < bottomY / 2) {
      slideUp()
    } else {
      slideDown()
    }
  }

  def moveTop(): Unit = {
    setY(topY)
  }

  def moveBottom(): Unit = {
    setY(bottomY)
  }


}
