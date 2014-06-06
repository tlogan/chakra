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

  val mainTextView: TextView = new MajorTextView(context, mainText)

  var secondTextView: TextView = new MinorTextView(context, secondText)

  var thirdTextView: TextView = new MinorTextView(context, thirdText)

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
