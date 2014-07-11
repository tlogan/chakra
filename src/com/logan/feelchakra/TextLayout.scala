package com.logan.feelchakra
import RichListView.listView2RichListView
import RichView.view2RichView
import RichContext.context2RichContext

trait TextLayout {
  this: LinearLayout => 

  val mainTextView: TextView
  val secondTextView: TextView
  val thirdTextView: TextView

  val fourthTextView: TextView
  val fifthTextView: TextView
  val sixthTextView: TextView

}

object TextLayout {

  def createTextLayout(
      context: Context, 
      mainText: String, 
      secondText: String, 
      thirdText: String,
      fourthText: String,
      fifthText: String,
      sixthText: String
  ): LinearLayout with TextLayout = {
    val v = new LinearLayout(context) with TextLayout {
      override val mainTextView: TextView = TextView.createMajor(context, mainText)
      override val secondTextView: TextView = TextView.createMinor(context, secondText)
      override val thirdTextView: TextView = TextView.createMinor(context, thirdText)

      override val fourthTextView: TextView = TextView.createMajor(context, fourthText)
      override val fifthTextView: TextView = TextView.createMinor(context, fifthText)
      override val sixthTextView: TextView = TextView.createMinor(context, sixthText)
    }

    v.setOrientation(VERTICAL)

    List(
      (v.mainTextView -> v.fourthTextView), 
      (v.secondTextView -> v.fifthTextView), 
      (v.thirdTextView -> v.sixthTextView)
    ).foreach(pair => {
      v.addView {
        val l = new LinearLayout(context)
        l.setOrientation(HORIZONTAL)
        pair._1.setLayoutParams(new LLLayoutParams(0, MATCH_PARENT, 20))
        l.addView(pair._1)
        pair._2.setLayoutParams(new LLLayoutParams(context.dp(48), MATCH_PARENT))
        l.addView(pair._2)
        l
      }
    })

    v
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
