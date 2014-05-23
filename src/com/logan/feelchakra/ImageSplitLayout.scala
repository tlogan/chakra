package com.logan.feelchakra
import RichListView.listView2RichListView
import RichView.view2RichView

class ImageSplitLayout(context: Context, rightLayout: View) extends LinearLayout(context) {

  val imageLayout: View = new View(context) {
    setBackgroundColor(WHITE)
    setLayoutParams(new LLLayoutParams(94, 94))
  }

  setOrientation(HORIZONTAL)
  setBackgroundColor(BLACK)
  addView(imageLayout)
  addView(rightLayout)


}
