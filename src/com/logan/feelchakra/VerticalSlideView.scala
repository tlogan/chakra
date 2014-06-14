package com.logan.feelchakra

import android.util.Log
import android.widget.Toast
import RichListView.listView2RichListView
import RichView.view2RichView

trait VerticalSlideView {
  this: View =>

  def velMs: Int
  def downY: Int
  def upY: Int 
  def onSlideUpEnd(): Unit
  def onSlideDownEnd(): Unit

  def slideUp(): Unit = {
    animate()
      .y(upY)
      .setDuration((getY().toInt - upY)/velMs)
      .setListener(new AnimatorListenerAdapter() {
        override def onAnimationEnd(animator: Animator): Unit = {
          onSlideUpEnd()
        }
      })
  }

  def slideDown(): Unit = {
    animate().y(downY).setDuration((downY - getY().toInt)/velMs)
      .setListener(new AnimatorListenerAdapter() {
        override def onAnimationEnd(animator: Animator): Unit = {
          onSlideDownEnd()
        }
      })
  }

  def slide(): Unit = {
    if (getY() < (downY + upY) / 2) {
      slideUp()
    } else {
      slideDown()
    }
  }

  def moveUp(): Unit = {
    setY(upY)
  }

  def moveUp(offset: Int): Unit = {
    setY(Math.max(offset, upY))
  }


  def moveDown(): Unit = {
    setY(downY)
  }

  def moveDown(offset: Int): Unit = {
    setY(Math.min(offset, downY))
  }



}
