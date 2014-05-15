package com.logan.feelchakra

class ImageTextLayout(
    context: Context, 
    mainText: String, 
    secondText: String, 
    thirdText: String
) extends LinearLayout(context) {

  var squareLayout: View = _
  var verticalLayout: LinearLayout = _
  var mainTextView: TextView = _
  var secondTextView: TextView = _
  var thirdTextView: TextView = _

  setOrientation(HORIZONTAL)
  setBackgroundColor(BLACK)
  addView {
    squareLayout = new View(context) {
      setBackgroundColor(WHITE)
      setLayoutParams(new LLLayoutParams(94, 94))
    }
    squareLayout
  }

  addView {
    verticalLayout = new LinearLayout(context) {
      setOrientation(VERTICAL)
      setBackgroundColor(DKGRAY)
      setLayoutParams(new LLLayoutParams(MATCH_PARENT, WRAP_CONTENT))
      addView {
        mainTextView = new TextView(context) {
          setText(mainText)
          setTextSize(20)
          setPadding(10, 2, 10, 3)
          setTextColor(WHITE)
        }
        mainTextView
      }

      addView {
        secondTextView = new TextView(context) {
          setText(secondText)
          setTextSize(14)
          setPadding(10, 0, 10, 0)
          setTextColor(GRAY)
        }
        secondTextView
      }

      addView {
        thirdTextView = new TextView(context) {
          setText(thirdText)
          setTextSize(14)
          setPadding(10, 0, 10, 0)
          setTextColor(GRAY)
        }
        thirdTextView
      }

    }
    verticalLayout
  }

  def blacken(): Unit = {
    verticalLayout.setBackgroundColor(BLACK)
    secondTextView.setTextColor(WHITE)
    thirdTextView.setTextColor(WHITE)
  }

  def darken(): Unit = {
    verticalLayout.setBackgroundColor(DKGRAY)
    secondTextView.setTextColor(GRAY)
    thirdTextView.setTextColor(GRAY)
  }

  def lighten(): Unit = {
    verticalLayout.setBackgroundColor(GRAY)
    secondTextView.setTextColor(LTGRAY)
    thirdTextView.setTextColor(LTGRAY)
  }

  def setTexts(mainText: String, secondText: String, thirdText: String): Unit = {
    mainTextView.setText(mainText)
    secondTextView.setText(secondText)
    thirdTextView.setText(thirdText)
  }




}
