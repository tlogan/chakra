package com.logan.feelchakra
import RichListView.listView2RichListView
import RichView.view2RichView
import RichContext.context2RichContext

trait TextLayout {
  this: LinearLayout => 

  val mainTextView: TextView
  val secondTextView: TextView
  val thirdTextView: TextView

}

object TextLayout {

  def createTextLayout(
      context: Context, 
      mainText: String, 
      secondText: String, 
      thirdText: String
  ): LinearLayout with TextLayout = {
    val v = new LinearLayout(context) with TextLayout {
      override val mainTextView: TextView = TextView.createMajor(context, mainText)
      override val secondTextView: TextView = TextView.createMinor(context, secondText)
      override val thirdTextView: TextView = TextView.createMinor(context, thirdText)
    }
    TextLayout.addTextViews(v)
    v
  }

  def addTextViews(view: LinearLayout with TextLayout): Unit = {
    view.setOrientation(VERTICAL)

    List(view.mainTextView, view.secondTextView, view.thirdTextView).foreach(textView => {
      view.addView(textView)
    })

  }

  def setTexts(
      textLayout: LinearLayout with TextLayout, 
      mainText: String, 
      secondText: String, 
      thirdText: String
  ): Unit = {
    textLayout.mainTextView.setText(mainText)
    textLayout.secondTextView.setText(secondText)
    textLayout.thirdTextView.setText(thirdText)
  }

}
