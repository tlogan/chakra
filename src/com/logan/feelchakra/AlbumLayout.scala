package com.logan.feelchakra
import RichListView.listView2RichListView
import RichView.view2RichView
import RichContext.context2RichContext

object AlbumLayout {
  def create(
      context: Context, 
      album: String,
      trackList: List[Track],
      futureTrackMap: Map[Track, Int],
      trackOption: Option[Track]
  ): LinearLayout = {

    val totalDuration = trackList.foldLeft[Long](0)((duration, track) => {
      duration + track.duration
    })

    val time = ms2MinSec(totalDuration)

    val v = new LinearLayout(context)
    val mainTextView = TextView.createMajor(context, album)
    val secondTextView = TextView.createMinor(context, trackList.size + " Tracks")
    val thirdTextView = TextView.createMinor(context, time)

    v.setOrientation(VERTICAL)
    List(mainTextView, secondTextView, thirdTextView).foreach(textView => {
      v.addView(textView)
    })

    v.setBackgroundColor(LDKGRAY)
    v.addView {
      new View(context) {
        setLayoutParams(new LLLayoutParams(MATCH_PARENT, context.dp(4)))
      }
    }
    v.setOnLongClick(view => {
      trackList.foreach(track => {
        mainActorRef ! MainActor.AppendFutureTrack(track)
      })
      true 
    })


    trackList.toIterator.zipWithIndex.foreach(pair => {
      val track = pair._1
      val trackNum = pair._2 + 1

      val current = trackOption match {
        case Some(currentTrack) if (currentTrack == track) => true
        case _ => false 
      }

      val textLayout = { 
        val l = new LinearLayout(context)
        l.setOrientation(HORIZONTAL)
        l.setPadding(context.dp(4), context.dp(6), context.dp(4), context.dp(6))
        l.addView {
          val v = new TextView(context)
          v.setLayoutParams(new LLLayoutParams(0, MATCH_PARENT, 20))
          v.setText(track.title)
          v.setTextSize(context.sp(10))
          v.setTextColor(WHITE)
          v
        }
        futureTrackMap.get(track) match {
          case Some(pos) =>
            l.addView {
              val tv = new TextView(context)
              tv.setLayoutParams(new LLLayoutParams(0, MATCH_PARENT, 1))
              tv.setText((pos + 1).toString)
              tv.setTextSize(context.sp(10))
              tv.setTextColor(WHITE)
              tv
            }
          case None =>
        }
        l
      }

      v.addView {
        if (current) {
          textLayout.setBackgroundColor(BLUE)
          textLayout.setLayoutParams(new LLLayoutParams(MATCH_PARENT, context.dp(40)))
          textLayout
        } else {

          val color = futureTrackMap.get(track) match {
            case Some(pos) => GRAY 
            case None => LDKGRAY 
          }
          val layout = SlideLayout.createAlbumTrackLayout(context, track, color, textLayout)
          layout.setLayoutParams(new LLLayoutParams(MATCH_PARENT, context.dp(40)))
          layout 
        }
      } 

      if (trackNum != trackList.size) {
        //add a white line after all album tracks except the last
        v.addView {
          new View(context) {
            setBackgroundColor(LTGRAY)
            val lp = new LLLayoutParams(MATCH_PARENT, 1)
            lp.setMargins(context.dp(8), 0, context.dp(8), 0)
            setLayoutParams(lp)
          }
        }
      }
    })

    v

  }
}
