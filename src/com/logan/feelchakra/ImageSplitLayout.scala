package com.logan.feelchakra
import RichListView.listView2RichListView
import RichView.view2RichView

class ImageSplitLayout(context: Context, rightLayout: View) extends LinearLayout(context) {

  val imageLayout: View = new View(context) {
    setBackgroundColor(WHITE)
    setLayoutParams(new LLLayoutParams(94, 94))
  }

  rightLayout.setLayoutParams(new LLLayoutParams(MATCH_PARENT, WRAP_CONTENT))

  setOrientation(HORIZONTAL)
  setBackgroundColor(BLACK)
  addView(imageLayout)
  addView(rightLayout)


}
