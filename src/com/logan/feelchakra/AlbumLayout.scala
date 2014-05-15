package com.logan.feelchakra

class AlbumLayout(
    context: Context, 
    album: String,
    trackList: List[Track]
) extends ImageTextLayout(context, album, trackList.size + " Tracks", "time") {

      verticalLayout.addView {
        new View(context) {
          setLayoutParams(new LLLayoutParams(MATCH_PARENT, 8))
        }
      }

      trackList.toIterator.zipWithIndex.foreach(pair => {
        val track = pair._1
        val trackNum = pair._2 + 1

        verticalLayout.addView {
          new LinearLayout(context) {
            setOrientation(VERTICAL)
            setPadding(10, 0, 10, 0)

            addView {
              new TextView(context) {
                setText(trackNum + ". " + track.title)
                setTextSize(18)
                setTextColor(WHITE)
                setPadding(0, 12, 0, 12)
              }
            }
            
          }
        }

        if (trackNum != trackList.size) {
          verticalLayout.addView {
            new View(context) {
              setBackgroundColor(LTGRAY)
              val lp = new LLLayoutParams(MATCH_PARENT, 1)
              lp.setMargins(10, 0, 10, 0)
              setLayoutParams(lp)
            }
          }
        }
      })


}
