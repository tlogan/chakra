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
            setLayoutParams(new LLLayoutParams(MATCH_PARENT, WRAP_CONTENT))

            addView {
              new TextView(context) {
                setText(trackNum + ". " + track.title)
                setTextSize(18)
                setTextColor(WHITE)
                setPadding(0, 6, 0, 6)
                setLayoutParams(new LLLayoutParams(MATCH_PARENT, WRAP_CONTENT))
              }
            }
            
          }
        }

        if (trackNum != trackList.size) {
          verticalLayout.addView {
            new View(context) {
              setBackgroundColor(WHITE)
              setLayoutParams(new LLLayoutParams(MATCH_PARENT, 1))
            }
          }
        }
      })


}
