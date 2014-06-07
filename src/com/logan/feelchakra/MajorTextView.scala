package com.logan.feelchakra
import RichListView.listView2RichListView
import RichView.view2RichView
import RichContext.context2RichContext

class MajorTextView(context: Context, text: String) extends TextView(context) {

  setText(text)
  setTextSize(18)
  setPadding(context.dp(4), context.dp(1), context.dp(4), context.dp(1))
  setTextColor(WHITE)

}