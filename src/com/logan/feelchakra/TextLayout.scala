package com.logan.feelchakra
import RichListView.listView2RichListView
import RichView.view2RichView
import RichContext.context2RichContext

class TextLayout(
    context: Context, 
    mainText: String, 
    secondText: String, 
    thirdText: String
) extends LinearLayout(context) {

  val mainTextView: TextView = new TextView(context) {
    setText(mainText)
    setTextSize(context.sp(12))
    setPadding(context.dp(4), context.dp(1), context.dp(4), context.dp(1))
    setTextColor(WHITE)
  }

  var secondTextView: TextView = new TextView(context) {
    setText(secondText)
    setTextSize(context.sp(8))
    setPadding(context.dp(4), 0, context.dp(4), 0)
    setTextColor(WHITE)
  }

  var thirdTextView: TextView = new TextView(context) {
    setText(thirdText)
    setTextSize(context.sp(8))
    setPadding(context.dp(4), 0, context.dp(4), 0)
    setTextColor(WHITE)
  }

  def setTexts(mainText: String, secondText: String, thirdText: String): Unit = {
    mainTextView.setText(mainText)
    secondTextView.setText(secondText)
    thirdTextView.setText(thirdText)
  }

  setOrientation(VERTICAL)
  addView(mainTextView)
  addView(secondTextView)
  addView(thirdTextView)

}
