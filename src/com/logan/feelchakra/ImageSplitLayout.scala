package com.logan.feelchakra

import RichListView.listView2RichListView
import RichView.view2RichView
import RichContext.context2RichContext

class ImageSplitLayout(context: Context, rightLayout: View) extends LinearLayout(context) {

  val imageLayout: View = new View(context) {
    setBackgroundColor(WHITE)
    setLayoutParams(new LLLayoutParams(context.dp(64), context.dp(64)))
  }

  rightLayout.setLayoutParams(new LLLayoutParams(MATCH_PARENT, WRAP_CONTENT))

  setOrientation(HORIZONTAL)
  setBackgroundColor(BLACK)
  addView(imageLayout)
  addView(rightLayout)


}
