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
    view.addView(view.mainTextView)
    view.addView(view.secondTextView)
    view.addView(view.thirdTextView)
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

  def createAlbumLayout(
      context: Context, 
      album: String,
      trackList: List[Track],
      playmap: Map[Track, TreeSet[Int]],
      trackOption: Option[Track]
  ): LinearLayout with TextLayout = {

    val v = new LinearLayout(context) with TextLayout {
      override val mainTextView: TextView = TextView.createMajor(context, album)
      override val secondTextView: TextView = TextView.createMinor(context, trackList.size + " Tracks")
      override val thirdTextView: TextView = TextView.createMinor(context, "---")
    }
    TextLayout.addTextViews(v)
    v.setBackgroundColor(LDKGRAY)
    v.addView {
      new View(context) {
        setLayoutParams(new LLLayoutParams(MATCH_PARENT, context.dp(4)))
      }
    }
    v.setOnLongClick(view => {
      trackList.foreach(track => {
        mainActorRef ! MainActor.AddPlaylistTrack(track)
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
        l.setOrientation(VERTICAL)
        l.setPadding(context.dp(4), context.dp(6), context.dp(4), context.dp(6))
        l.setBackgroundColor(TRANSPARENT)
        l.addView {
          val v = new TextView(context)
          v.setText(trackNum + ". " + track.title)
          v.setTextSize(context.sp(10))
          v.setTextColor(WHITE)
          v
        }
        l
      }

      v.addView {
        if (current) {
          textLayout.setBackgroundColor(BLUE)
          textLayout
        } else {
          val layout = SlideLayout.createAlbumTrackLayout(context, track, playmap.get(track), textLayout)
          layout.setLayoutParams(new LLLayoutParams(MATCH_PARENT, context.dp(40)))
          layout 
        }
      } 

      if (trackNum != trackList.size) {
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
