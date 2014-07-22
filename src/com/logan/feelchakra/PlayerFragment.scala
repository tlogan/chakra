package com.logan.feelchakra

import RichListView.listView2RichListView
import RichContext.context2RichContext
import android.util.Log
import android.widget.Toast

object PlayerFragment {

  def create(slideLayout: LinearLayout with HorizontalSlideView): Fragment = {

    new Fragment() {

      override def onCreate(savedState: Bundle): Unit = {
        super.onCreate(savedState)
      }

      override def onCreateView(inflater: LayoutInflater, viewGroup: ViewGroup, savedState: Bundle): View = {

        def createTrackTextLayout() = {
          val t = TextLayout.createTextLayout(getActivity(), "", "", "", "", "", "") 
          t.setLayoutParams(new RLLayoutParams(MATCH_PARENT, WRAP_CONTENT))
          t
        }

        val playerTextLayout = { 
          val v = createTrackTextLayout()
          v.setBackgroundColor(TRANSPARENT)
          v
        }
        val nextTextLayout = { 
          val v = createTrackTextLayout()
          v.setBackgroundColor(GRAY)
          v
        }
        val prevTextLayout = {
          val v = createTrackTextLayout()
          v.setBackgroundColor(DKGRAY)
          v
        }

        val backBar = new View(getActivity()) {
          setBackgroundColor(BLUE)
          setLayoutParams(new RLLayoutParams(MATCH_PARENT, MATCH_PARENT))
        }

        val frontBar = new View(getActivity()) {
          setBackgroundColor(DKBLUE)
          setLayoutParams(new RLLayoutParams(MATCH_PARENT, MATCH_PARENT))
        }

        val playerProgressView = new RelativeLayout(getActivity()) {
          addView(backBar)
          addView(frontBar)
          addView(playerTextLayout)
        }

        val width = dimension(getActivity()).x

        val prevLayout =  {
          val layout = ImageSplitLayout.createMain(getActivity(), null, prevTextLayout)
          layout.setLayoutParams(new LLLayoutParams(width, WRAP_CONTENT))
          layout
        }

        val playerLayout =  {
          val l = ImageSplitLayout.createMain(getActivity(), null, playerProgressView)
          l.setLayoutParams(new LLLayoutParams(width, WRAP_CONTENT))
          l
        }

        val nextLayout =  {
          val l = ImageSplitLayout.createMain(getActivity(), null, nextTextLayout) 
          l.setLayoutParams(new LLLayoutParams(width, WRAP_CONTENT))
          l
        }

        slideLayout.addView(prevLayout)
        slideLayout.addView(playerLayout)
        slideLayout.addView(nextLayout)

        def convertAdapter(adapter: ListAdapter, f: BaseAdapter with PlaylistAdapter => Unit): Unit = {
          adapter match {
            case adapter: BaseAdapter with PlaylistAdapter => {
              f(adapter)
            }
          } 
        }

        val playlistView: ListView = {
          val lv = ListView.createMain(this.getActivity(), PlaylistAdapter.create(this.getActivity())) 
          lv.setLayoutParams(new LLLayoutParams(MATCH_PARENT, MATCH_PARENT)) 
          lv.setOnItemClick( 
            (parent: AdapterView[_], view: View, position: Int, id: Long) => {
              convertAdapter(lv.getAdapter(), adapter => {
                if (adapter.isPast(position)) {
                  val index = adapter.pastIndex(position)
                  mainActorRef ! MainActor.SetPresentTrackFromPastIndex(index) 
                } else {
                  val index = adapter.futureIndex(position)
                  mainActorRef ! MainActor.SetPresentTrackFromFutureIndex(index) 
                }
              })
            }
          )
          lv
        }
         
        var _trackDuration: Long = -1
        var _playing = false 
        var _startPos: Long = 0 
        var _startTime: Long = 0 

        var _playState: PlayState = NotPlaying

        var _stationConnection: StationConnection = StationDisconnected


        def stopProgress(): Unit = {
          frontBar.animate().cancel()
        }

        def animateProgress(): Unit = {
          assert(_trackDuration >= _startPos)

          val width = playerProgressView.getWidth()
          frontBar.animate()
            .x(width)
            .setDuration(_trackDuration - _startPos)
        }


        def withAdapter(f: PlaylistAdapter => Unit): Unit = {
          convertAdapter(playlistView.getAdapter(), f)
        }

        val handler = new Handler(new HandlerCallback() {
          override def handleMessage(msg: Message): Boolean = {
            import UI._ 
            msg.obj match {


              case OnStationConnectionChanged(stationConnection) =>
                _stationConnection = stationConnection
                stationConnection match {
                  case StationDisconnected =>
                    playlistView.setVisibility(VISIBLE)
                  case _ => 
                    playlistView.setVisibility(GONE)
                }
                true

              case OnPlayerOpenChanged(playerOpen) => 

                if (playerOpen) { 
                  withAdapter(adapter => {
                    val pos = adapter.firstFuturePosition
                    playlistView.setSelectionFromTop(pos, 0)
                  })
                }
                true

              case OnStationPlayStateChanged(playState) =>
                Log.d("chakra", "OnStationPlayStateChanged: " + playState)
                _playState = playState
                playState match {
                  case Playing(startTime) => 
                    if (_trackDuration >= 0) {
                      val width = playerProgressView.getWidth()
                      frontBar.setX(
                        (_startPos + (Platform.currentTime - startTime).toInt) * width/_trackDuration
                      ) 
                      animateProgress()
                    } else {
                      stopProgress()
                    }
                  case _ =>
                }

                true

              case OnStationTrackOpChanged(trackOption) => 
                trackOption match {
                  case Some(track) =>
                    _trackDuration = track.duration
                    _playState match {
                      case Playing(startTime) => 
                        if (_trackDuration >= 0) {
                          val width = playerProgressView.getWidth()
                          frontBar.setX(
                            (_startPos + (Platform.currentTime - startTime).toInt) * width/_trackDuration
                          ) 
                          animateProgress()
                        } else {
                          stopProgress()
                        }
                      case _ =>
                    }
                    TextLayout.setTexts(playerTextLayout, track.title, track.artist, track.album.title)
                    playerLayout.imageLayout.setImageDrawable(track.album.coverArt)
                  case _ => 
                    TextLayout.setTexts(playerTextLayout, "", "", "")
                    playerLayout.imageLayout.setImageDrawable(null)
                }
                true

              case OnLocalPlayingChanged(playing) if _stationConnection == StationDisconnected =>
                Log.d("chakra", "OnLocalPlayingChanged: " + playing)
                _playing = playing
                if (_playing) {
                  animateProgress()
                } else {
                  _startPos = _startPos + Platform.currentTime - _startTime
                  _startTime = Platform.currentTime
                  stopProgress()
                }
                true
              case OnLocalStartPosChanged(startPos) if _stationConnection == StationDisconnected =>
                Log.d("chakra", "OnLocalStartPosChanged: " + startPos)
                _startPos = startPos 
                _startTime = Platform.currentTime
                if (_trackDuration > -1) {
                  val width = playerProgressView.getWidth()
                  frontBar.setX(startPos * width/_trackDuration)
                }
                true
              case OnPastTrackListChanged(list) if _stationConnection == StationDisconnected => 
                withAdapter(adapter => {
                  adapter.setPastTrackList(list)
                })
                list.lastOption match {
                  case Some(track) =>
                    prevTextLayout.sixthTextView.setText(ms2MinSec(track.duration))
                    TextLayout.setTexts(prevTextLayout, track.title, track.artist, track.album.title)
                    prevLayout.imageLayout.setImageDrawable(track.album.coverArt)
                  case _ => 
                    prevTextLayout.sixthTextView.setText("")
                    TextLayout.setTexts(prevTextLayout, "", "", "")
                    prevLayout.imageLayout.setImageDrawable(null)
                }
                true
              case OnPresentTrackOptionChanged(trackOption) if _stationConnection == StationDisconnected => 
                trackOption match {
                  case Some(track) =>
                    _trackDuration = track.duration
                    if (_trackDuration >= 0 && _playing) {
                      animateProgress()
                    } else {
                      stopProgress()
                    }
                    
                    playerTextLayout.sixthTextView.setText(ms2MinSec(track.duration))
                    TextLayout.setTexts(playerTextLayout, track.title, track.artist, track.album.title)
                    playerLayout.imageLayout.setImageDrawable(track.album.coverArt)
                  case _ => 
                    playerTextLayout.sixthTextView.setText("")
                    TextLayout.setTexts(playerTextLayout, "", "", "")
                    playerLayout.imageLayout.setImageDrawable(null)
                }
                true
              case OnFutureTrackListChanged(list) if _stationConnection == StationDisconnected => 
                withAdapter(adapter => {
                  adapter.setFutureTrackList(list)
                })
                list.headOption match {
                  case Some(track) =>
                    nextTextLayout.sixthTextView.setText(ms2MinSec(track.duration))
                    TextLayout.setTexts(nextTextLayout, track.title, track.artist, track.album.title)
                    nextLayout.imageLayout.setImageDrawable(track.album.coverArt)
                  case _ => 
                    nextTextLayout.sixthTextView.setText("")
                    TextLayout.setTexts(nextTextLayout, "", "", "")
                    nextLayout.imageLayout.setImageDrawable(null)
                }
                true
              case _ => false
            }
           false
          }
        })

        mainActorRef ! MainActor.Subscribe(this.toString, handler) 

        val ll = new LinearLayout(this.getActivity())
        ll.setOrientation(VERTICAL)
        ll.addView(slideLayout)
        slideLayout.setX(-width)
        ll.addView(playlistView)
        ll
       
      }

      override def onDestroy(): Unit =  {
        super.onDestroy()
        mainActorRef ! MainActor.Unsubscribe(this.toString)
      }

    }

  }

}
