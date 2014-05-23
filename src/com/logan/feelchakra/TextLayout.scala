package com.logan.feelchakra
import RichListView.listView2RichListView
import RichView.view2RichView

class TextLayout(
    context: Context, 
    mainText: String, 
    secondText: String, 
    thirdText: String
) extends LinearLayout(context) {

  val mainTextView: TextView = new TextView(context) {
    setText(mainText)
    setTextSize(20)
    setPadding(10, 2, 10, 3)
    setTextColor(WHITE)
  }

  var secondTextView: TextView = new TextView(context) {
    setText(secondText)
    setTextSize(14)
    setPadding(10, 0, 10, 0)
    setTextColor(WHITE)
  }

  var thirdTextView: TextView = new TextView(context) {
    setText(thirdText)
    setTextSize(14)
    setPadding(10, 0, 10, 0)
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
