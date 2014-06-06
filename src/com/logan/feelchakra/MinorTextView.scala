package com.logan.feelchakra
import RichListView.listView2RichListView
import RichView.view2RichView
import RichContext.context2RichContext

class MinorTextView(context: Context, text: String) extends TextView(context) {

  setText(text)
  setTextSize(context.sp(8))
  setPadding(context.dp(4), 0, context.dp(4), 0)
  setTextColor(WHITE)

}
