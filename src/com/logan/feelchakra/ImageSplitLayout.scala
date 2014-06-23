package com.logan.feelchakra

import RichListView.listView2RichListView
import RichView.view2RichView
import RichContext.context2RichContext

trait ImageSplitLayout {
  this: ViewGroup =>

  val imageLayout: View
  val rightLayout: View
}

object ImageSplitLayout {

  def construct(layout: LinearLayout with ImageSplitLayout): Unit = {
    layout.setOrientation(HORIZONTAL)
    layout.setBackgroundColor(BLACK)
    layout.addView(layout.imageLayout)
    layout.addView(layout.rightLayout)
  }

  def create(context: Context, rightView: View): LinearLayout with ImageSplitLayout = {

    val layout = new LinearLayout(context) with ImageSplitLayout {

      override val imageLayout: View = ImageSplitLayout.imageLayout(context)

      override val rightLayout = {
        rightView.setLayoutParams(new LLLayoutParams(MATCH_PARENT, WRAP_CONTENT))
        rightView 
      }
    }
    ImageSplitLayout.construct(layout)
    layout

  }

  def imageLayout(context: Context): View = {
    val view = new View(context)
    view.setBackgroundColor(WHITE)
    view.setLayoutParams(new LLLayoutParams(context.dp(64), context.dp(64)))
    view
  }

  def createMain(context: Context, rightView: View): LinearLayout with ImageSplitLayout = {
    val layout = new LinearLayout(context) with ImageSplitLayout {

      override val imageLayout: View = ImageSplitLayout.imageLayout(context)

      override val rightLayout = {
        rightView.setLayoutParams(new LLLayoutParams(MATCH_PARENT, context.dp(medDp)))
        rightView 
      }
    }
    ImageSplitLayout.construct(layout)
    layout
  }

}
