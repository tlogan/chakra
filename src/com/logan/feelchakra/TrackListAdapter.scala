package com.logan.feelchakra

import android.util.Log
import android.widget.Toast

import RichListView.listView2RichListView
import RichView.view2RichView

class TrackListAdapter(activity: Activity) extends BaseAdapter {

  private var _trackList: List[Track] = List() 

  private var _playmap: Map[Track, List[Int]] = HashMap() 
  private var _trackOption: Option[Track] = None 

  override def getCount(): Int = _trackList.size

  override def getItem(position: Int): Track = _trackList(getItemId(position).toInt)

  override def getItemId(position: Int): Long = position 

  override def getView(position: Int, view: View, viewGroup: ViewGroup): View = {

    val track = getItem(position)

    new ImageSplitLayout(activity, new RelativeLayout(activity) {

      setLayoutParams(new LLLayoutParams(MATCH_PARENT, WRAP_CONTENT))

      val textLayout = new TextLayout(activity, track.title, track.artist, track.album) {
        setBackgroundColor(TRANSPARENT)
        setLayoutParams(new RLLayoutParams(MATCH_PARENT, WRAP_CONTENT))
      }

      lazy val width = textLayout.getWidth()
      lazy val height = textLayout.getHeight()

      val slideView = new View(activity) with HorizontalSlideView {
        override val velMs = 2
        override val left = 0
        override lazy val right = width
        override def onSlideRightEnd() = mainActorRef !  MainActor.AddAndPlayTrack(track) 
        override def onSlideLeftEnd() = {}

        val color = _playmap.get(track) match {
          case Some(posList) => 
            _trackOption match {
              case Some(currentTrack) if (currentTrack == track) =>
                BLUE 
              case _ => GRAY 
            }
          case None => DKGRAY 
        }
        setBackgroundColor(color)

        setLayoutParams(new RLLayoutParams(MATCH_PARENT, 100))

      }

      val veiledView = new View(activity) {
        setBackgroundColor(BLUE)
        setLayoutParams(new RLLayoutParams(MATCH_PARENT, 100))
      }

      addView(veiledView)
      addView(slideView)
      addView(textLayout)
      bringChildToFront(textLayout)

      val gestureDetector = new GestureDetector(getContext(), new SimpleOnGestureListener {

        override def onDown(e: MotionEvent): Boolean = {
          true
        }

        override def onSingleTapUp(e: MotionEvent): Boolean = {
          mainActorRef ! MainActor.AddPlaylistTrack(track)
          true
        }

        override def onScroll(e1: MotionEvent, e2: MotionEvent, distX: Float, distY: Float): Boolean = {
          val totalDispX = e2.getX().toInt - e1.getX().toInt 
          if (totalDispX > 0) {
            slideView.setX(totalDispX)
          }
          true
        }

        override def onFling(e1: MotionEvent, e2: MotionEvent, velX: Float, velY: Float): Boolean = {
          if (velX > 0) {
            slideView.slideRight()
          } else {
            slideView.slideLeft()
          }
          true
        }
      })

      this.setOnTouch((view, event) => {

        if (!gestureDetector.onTouchEvent(event)) {
          event.getAction() match {
            case ACTION_UP => 
              slideView.slide()
              true
            case ACTION_CANCEL => 
              slideView.slide()
              true
            case _ =>
              false
          }
        } else true 

      })


    })



  }

  def setTrackList(trackList: List[Track]): Unit = {
    _trackList = trackList
    this.notifyDataSetChanged()
  }

  def setPlaymap(playmap: Map[Track, List[Int]]): Unit = {
    _playmap = playmap
    this.notifyDataSetChanged()
  }

  def setTrackOption(trackOption: Option[Track]): Unit = {
    _trackOption = trackOption 
    this.notifyDataSetChanged()
  }

}
