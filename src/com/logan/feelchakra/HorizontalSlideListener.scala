package com.logan.feelchakra

import android.util.Log
import android.widget.Toast
import RichListView.listView2RichListView
import RichView.view2RichView

class HorizontalSlideListener(slideView: View with HorizontalSlideView) extends SimpleOnGestureListener {

  override def onDown(e: MotionEvent): Boolean = {
    true
  }

  override def onScroll(e1: MotionEvent, e2: MotionEvent, distX: Float, distY: Float): Boolean = {
    val totalDispX = e2.getX().toInt - e1.getX().toInt 
    if (totalDispX > 0) {
      slideView.setX(totalDispX)
    }
    true
  }

  override def onFling(e1: MotionEvent, e2: MotionEvent, velX: Float, velY: Float): Boolean = {
    if (velX > 0) {
      slideView.slideRight()
    } else {
      slideView.slideLeft()
    }
    true
  }

}
