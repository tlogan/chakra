package com.logan.feelchakra
import RichListView.listView2RichListView
import RichView.view2RichView

class AlbumLayout(
    context: Context, 
    album: String,
    trackList: List[Track]
) extends ImageTextLayout(context, album, trackList.size + " Tracks", "time") {

      this.setOnClick(view => {
        trackList.foreach(track => {
          mainActorRef ! MainActor.AddPlaylistTrack(track)
        })
      })

      verticalLayout.addView {
        new View(context) {
          setLayoutParams(new LLLayoutParams(MATCH_PARENT, 8))
        }
      }

      trackList.toIterator.zipWithIndex.foreach(pair => {
        val track = pair._1
        val trackNum = pair._2 + 1

        verticalLayout.addView {
          val trackLL = new LinearLayout(context) {
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
          trackLL.setOnClick(view => {
            mainActorRef ! MainActor.AddPlaylistTrack(track)
          })

          trackLL
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
