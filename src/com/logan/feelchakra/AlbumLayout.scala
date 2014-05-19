package com.logan.feelchakra
import RichListView.listView2RichListView
import RichView.view2RichView

class AlbumLayout(
    context: Context, 
    album: String,
    trackList: List[Track],
    playmap: Map[Track, List[Int]],
    trackOption: Option[Track]
) extends ImageTextLayout(
    context, 
    album, 
    trackList.size + " Tracks", 
    "time",
    DKGRAY
) {

  verticalLayout.setOnLongClick(view => {
    trackList.foreach(track => {
      mainActorRef ! MainActor.AddPlaylistTrack(track)
    })
    true 
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
        setPadding(10, 12, 10, 12)

        addView {
          new TextView(context) {
            setText(trackNum + ". " + track.title)
            setTextSize(18)
            setTextColor(WHITE)
          }
        }

        playmap.get(track) match {
          case Some(posList) =>
            trackOption match {
              case Some(currentTrack) if (currentTrack == track) =>
                setBackgroundColor(BLUE) 
              case _ => setBackgroundColor(GRAY) 
            }
            addView {
              new TextView(context) {
                setText(posList.mkString(", "))
                setTextSize(14)
                setTextColor(LTGRAY)
              }
            }
          case None =>
            setBackgroundColor(DKGRAY) 
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
