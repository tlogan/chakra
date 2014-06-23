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

}

object VerticalSlideView {

  def slideUp(view: View with VerticalSlideView): Unit = {
    view.animate()
      .y(view.upY)
      .setDuration((view.getY().toInt - view.upY)/view.velMs)
      .setListener(new AnimatorListenerAdapter() {
        override def onAnimationEnd(animator: Animator): Unit = {
          view.onSlideUpEnd()
        }
      })
  }

  def slideDown(view: View with VerticalSlideView): Unit = {
    view.animate().y(view.downY).setDuration((view.downY - view.getY().toInt)/view.velMs)
      .setListener(new AnimatorListenerAdapter() {
        override def onAnimationEnd(animator: Animator): Unit = {
          view.onSlideDownEnd()
        }
      })
  }

  def slide(view: View with VerticalSlideView): Unit = {
    if (view.getY() < (view.downY + view.upY) / 2) {
      VerticalSlideView.slideUp(view)
    } else {
      VerticalSlideView.slideDown(view)
    }
  }

  def moveUp(view: View with VerticalSlideView): Unit = {
    view.setY(view.upY)
  }

  def moveUp(view: View with VerticalSlideView, offset: Int): Unit = {
    view.setY(Math.max(offset, view.upY))
  }


  def moveDown(view: View with VerticalSlideView): Unit = {
    view.setY(view.downY)
  }

  def moveDown(view: View with VerticalSlideView, offset: Int): Unit = {
    view.setY(Math.min(offset, view.downY))
  }
}
