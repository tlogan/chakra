package com.logan.feelchakra
import RichListView.listView2RichListView
import RichView.view2RichView
import RichContext.context2RichContext

object TextView {
  def createMinor(context: Context, text: String): TextView = {
    val tv = new TextView(context)
    tv.setText(text)
    tv.setTextSize(12)
    tv.setPadding(context.dp(4), context.dp(1), context.dp(4), context.dp(1))
    tv.setTextColor(WHITE)
    tv
  }

  def createMajor(context: Context, text: String): TextView = {
    val tv = new TextView(context)
    tv.setText(text)
    tv.setTextSize(18)
    tv.setPadding(context.dp(4), context.dp(1), context.dp(4), context.dp(1))
    tv.setTextColor(WHITE)
    tv
  }
}
