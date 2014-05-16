package com.logan.feelchakra

class ImageTextLayout(
    context: Context, 
    mainText: String, 
    secondText: String, 
    thirdText: String,
    color: Int 
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

  val textColor = color match {
    case BLACK => WHITE
    case DKGRAY => GRAY
    case GRAY => LTGRAY
    case BLUE => WHITE
  }

  addView {
    verticalLayout = new LinearLayout(context) {
      setOrientation(VERTICAL)
      setBackgroundColor(color)
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
          setTextColor(textColor)
        }
        secondTextView
      }

      addView {
        thirdTextView = new TextView(context) {
          setText(thirdText)
          setTextSize(14)
          setPadding(10, 0, 10, 0)
          setTextColor(textColor)
        }
        thirdTextView
      }

    }
    verticalLayout
  }

  def setTexts(mainText: String, secondText: String, thirdText: String): Unit = {
    mainTextView.setText(mainText)
    secondTextView.setText(secondText)
    thirdTextView.setText(thirdText)
  }




}
