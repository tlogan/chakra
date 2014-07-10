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

  def create(context: Context, image: Drawable, rightView: View): LinearLayout with ImageSplitLayout = {

    val layout = new LinearLayout(context) with ImageSplitLayout {

      override val imageLayout = ImageSplitLayout.imageLayout(context, image)

      override val rightLayout = {
        rightView.setLayoutParams(new LLLayoutParams(MATCH_PARENT, WRAP_CONTENT))
        rightView 
      }
    }
    ImageSplitLayout.construct(layout)
    layout

  }

  def imageLayout(context: Context, image: Drawable): ImageView = {
    val view = new ImageView(context)
    view.setImageDrawable(image)
    view.setBackgroundColor(TRANSPARENT)
    view.setLayoutParams(new LLLayoutParams(context.dp(64), context.dp(64)))
    view
  }

  def createMain(context: Context, image: Drawable, rightView: View): LinearLayout with ImageSplitLayout = {
    val layout = new LinearLayout(context) with ImageSplitLayout {

      override val imageLayout = ImageSplitLayout.imageLayout(context, image)

      override val rightLayout = {
        rightView.setLayoutParams(new LLLayoutParams(MATCH_PARENT, context.dp(medDp)))
        rightView 
      }
    }
    ImageSplitLayout.construct(layout)
    layout
  }

}
