package com.logan.feelchakra
import RichListView.listView2RichListView
import RichView.view2RichView

class ImageTextLayout(
    context: Context, 
    mainText: String, 
    secondText: String, 
    thirdText: String,
    color: Int 
) extends LinearLayout(context) {

  val textColor = color match {
    case BLACK => WHITE
    case DKGRAY => GRAY
    case GRAY => LTGRAY
    case BLUE => WHITE
  }

  val imageLayout: View = new View(context) {
    setBackgroundColor(WHITE)
    setLayoutParams(new LLLayoutParams(94, 94))
  }

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
    setTextColor(textColor)
  }

  var thirdTextView: TextView = new TextView(context) {
    setText(thirdText)
    setTextSize(14)
    setPadding(10, 0, 10, 0)
    setTextColor(textColor)
  }

  val textLayout: LinearLayout = new LinearLayout(context) {
    setOrientation(VERTICAL)
    setBackgroundColor(color)
    setLayoutParams(new LLLayoutParams(MATCH_PARENT, WRAP_CONTENT))

    addView(mainTextView)
    addView(secondTextView)
    addView(thirdTextView)
  }

  setOrientation(HORIZONTAL)
  setBackgroundColor(BLACK)

  addView(imageLayout)
  addView(textLayout)

  def setTexts(mainText: String, secondText: String, thirdText: String): Unit = {
    mainTextView.setText(mainText)
    secondTextView.setText(secondText)
    thirdTextView.setText(thirdText)
  }

  def setOnTextLayoutClick(f: View => Unit): Unit = {
    textLayout.setOnClick(f)
  }

}
