package com.logan.feelchakra

class ImageTextLayout(
    context: Context, 
    mainText: String, 
    secondText: String, 
    thirdText: String
) extends LinearLayout(context) {

  var squareLayout: View = _
  var verticalLayout: LinearLayout = _

  setOrientation(HORIZONTAL)
  setLayoutParams(new LLLayoutParams(MATCH_PARENT, WRAP_CONTENT))
  addView {
    squareLayout = new View(context) {
      setBackgroundColor(YELLOW)
      setLayoutParams(new LLLayoutParams(90, 90))
    }
    squareLayout
  }

  addView {
    verticalLayout = new LinearLayout(context) {
      setOrientation(VERTICAL)
      setPadding(10, 0, 10, 0)
      addView {
        new TextView(context) {
          setText(mainText)
          setTextSize(20)
          setTextColor(WHITE)
        }
      }

      addView {
        new TextView(context) {
          setText(secondText)
          setTextSize(14)
          setTextColor(GRAY)
        }
      }

      addView {
        new TextView(context) {
          setText(thirdText)
          setTextSize(14)
          setTextColor(GRAY)
        }
      }

    }
    verticalLayout
  }

}
