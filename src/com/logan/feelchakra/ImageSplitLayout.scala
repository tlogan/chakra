package com.logan.feelchakra

import RichListView.listView2RichListView
import RichView.view2RichView
import RichContext.context2RichContext

trait ImageSplitLayout {
  this: ViewGroup =>

  val imageLayout: ImageView
  val rightLayout: View
}

object ImageSplitLayout {

  def construct(layout: LinearLayout with ImageSplitLayout): Unit = {
    layout.setOrientation(HORIZONTAL)
    layout.setBackgroundColor(BLACK)
    layout.addView(layout.imageLayout)
    layout.addView(layout.rightLayout)
  }

  def create(context: Context, imagePath: String, rightView: View): LinearLayout with ImageSplitLayout = {

    val layout = new LinearLayout(context) with ImageSplitLayout {

      override val imageLayout = ImageSplitLayout.imageLayout(context, imagePath)

      override val rightLayout = {
        rightView.setLayoutParams(new LLLayoutParams(MATCH_PARENT, WRAP_CONTENT))
        rightView 
      }
    }
    ImageSplitLayout.construct(layout)
    layout

  }

  def setImageFromPath(layout: LinearLayout with ImageSplitLayout, imagePath: String): Unit = {
    layout.imageLayout.setImageDrawable(createDrawableFromPath(imagePath))
  }

  def imageLayout(context: Context, imagePath: String): ImageView = {
    val view = new ImageView(context)
    view.setImageDrawable(createDrawableFromPath(imagePath))
    view.setBackgroundColor(WHITE)
    view.setLayoutParams(new LLLayoutParams(context.dp(64), context.dp(64)))
    view
  }

  def createMain(context: Context, imagePath: String, rightView: View): LinearLayout with ImageSplitLayout = {
    val layout = new LinearLayout(context) with ImageSplitLayout {

      override val imageLayout = ImageSplitLayout.imageLayout(context, imagePath)

      override val rightLayout = {
        rightView.setLayoutParams(new LLLayoutParams(MATCH_PARENT, context.dp(medDp)))
        rightView 
      }
    }
    ImageSplitLayout.construct(layout)
    layout
  }

}
