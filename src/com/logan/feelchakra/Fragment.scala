package com.logan.feelchakra

import RichListView.listView2RichListView
import RichContext.context2RichContext
import android.util.Log
import android.widget.Toast

object Fragment {

  def createListFragment(createHandler: ListView => Handler, createListView: Context => ListView): Fragment = {

    new Fragment() {

      override def onCreate(savedState: Bundle): Unit = {
        super.onCreate(savedState)
      }

      override def onCreateView(inflater: LayoutInflater, viewGroup: ViewGroup, savedState: Bundle): View = {

        val listView = createListView(this.getActivity())
        mainActorRef ! MainActor.Subscribe(this.toString, createHandler(listView)) 

        val layout = new LinearLayout(this.getActivity())
        layout.setOrientation(VERTICAL)
        layout.addView(listView)
        layout

      }

      override def onDestroy(): Unit =  {
        super.onDestroy()
        mainActorRef ! MainActor.Unsubscribe(this.toString)
      }

    }

  }

  def createTrackSelection(): Fragment = {

    def convertAdapter(adapter: ListAdapter, f: BaseAdapter with TrackListAdapter => Unit): Unit = {
      adapter match {
        case adapter: BaseAdapter with TrackListAdapter => {
          f(adapter)
        }
      } 
    }

    def createHandler(listView: ListView) = {

      new Handler(new HandlerCallback() {
        override def handleMessage(msg: Message): Boolean = {

          val adapter = listView.getAdapter()
          def withAdapter(f: TrackListAdapter => Unit): Unit = {
            convertAdapter(adapter, f)
          }

          import UI._
          msg.obj match {
            case OnTrackListChanged(trackList) => 
              withAdapter(_.setTrackList(trackList))
              true
            case OnPlaylistChanged(playlist) => 
              withAdapter(_.setPlaymap(Playmap(playlist)))
              true
            case OnLocalTrackOptionChanged(trackOption) => 
              withAdapter(_.setTrackOption(trackOption))
              true
            case _ => false
          }
        }
      })

    }

    def createListView(context: Context): ListView = {
      val lv = ListView.createMain(context, TrackListAdapter.create(context))
      lv.setOnItemClick( 
        (parent: AdapterView[_], view: View, position: Int, id: Long) => {
          convertAdapter(lv.getAdapter(), adapter => {
            val track =  adapter.getItem(position)
            mainActorRef ! MainActor.AddPlaylistTrack(track) 
          })
        }
      ) 
      lv 
    }

    Fragment.createListFragment(createHandler, createListView)

  }

  def createArtistSelection(): Fragment = {

    def createHandler(listView: ListView) = {
      new Handler(new HandlerCallback() {
        override def handleMessage(msg: Message): Boolean = {

          val adapter = listView.getAdapter()
          def withAdapter(f: ArtistListAdapter => Unit): Unit = {
            adapter match {
              case adapter: ArtistListAdapter => {
                f(adapter)
              }
            } 
          }

          import UI._
          msg.obj match {
            case OnArtistMapChanged(artistMap) => 
              withAdapter(_.setArtistMap(artistMap))
              true
            case OnArtistTupleOpChanged(artistTupleOp) =>
              withAdapter(artistAdapter => {
                artistAdapter.setArtistTupleOp(artistTupleOp)
                if (artistTupleOp != None) {
                  val pos = artistAdapter.artistTuplePosition
                  listView.setSelectionFromTop(pos, 0)
                }
              })
              true
            case OnPlaylistChanged(playlist) => 
              withAdapter(_.setPlaymap(Playmap(playlist)))
              true
            case OnLocalTrackOptionChanged(trackOption) => 
              withAdapter(_.setTrackOption(trackOption))
              true
            case _ => false
          }
        }
      })
    }

    def createListView(context: Context): ListView = { 
      val lv =  ListView.createMain(context, ArtistListAdapter.create(context))
      lv.setLayoutParams(new LLLayoutParams(MATCH_PARENT, WRAP_CONTENT))
      lv
    }

    Fragment.createListFragment(createHandler, createListView)

  }

  def createAlbumSelection(): Fragment = {

    def createHandler(listView: ListView) = {
      new Handler(new HandlerCallback() {
        override def handleMessage(msg: Message): Boolean = {

          val adapter = listView.getAdapter()
          def withAdapter(f: AlbumListAdapter => Unit): Unit = {
            adapter match {
              case adapter: AlbumListAdapter => {
                f(adapter)
              }
            } 
          }

          import UI._
          msg.obj match {
            case OnAlbumMapChanged(albumMap) => 
              withAdapter(_.setAlbumMap(albumMap))
              true
            case OnAlbumTupleOpChanged(albumTupleOp) =>
              withAdapter(adapter => {
                adapter.setAlbumTupleOp(albumTupleOp)
                if (albumTupleOp != None) {
                  val pos = adapter.albumTuplePosition
                  listView.setSelectionFromTop(pos, 0)
                }
              })
              true
            case OnPlaylistChanged(playlist) => 
              withAdapter(_.setPlaymap(Playmap(playlist)))
              true
            case OnLocalTrackOptionChanged(trackOption) => 
              withAdapter(_.setTrackOption(trackOption))
              true
            case _ => false
          }

        }
      })
    }

    def createListView(context: Context): ListView = ListView.createMain(context, AlbumListAdapter.create(context))

    Fragment.createListFragment(createHandler, createListView)

  }

  def createStationSelection(): Fragment = {

    def convertAdapter(adapter: ListAdapter, f: StationListAdapter => Unit): Unit = {
      adapter match {
        case adapter: StationListAdapter => {
          f(adapter)
        }
      } 
    }

    def createHandler(listView: ListView) = {

      val adapter = listView.getAdapter() 

      def withAdapter(f: StationListAdapter => Unit): Unit = {
        convertAdapter(adapter, f)
      }

      new Handler(new HandlerCallback() {
        override def handleMessage(msg: Message): Boolean = {
          import UI._
          msg.obj match {
            case OnStationListChanged(stationList) => 
              withAdapter(_.setStationList(stationList))
              true
            case OnStationOptionChanged(stationOption) => 
              true
            case _ => false
          }
        }
      })
    }

    def createListView(context: Context): ListView = {
      val lv = ListView.createMain(context, StationListAdapter.create(context))
      lv.setOnItemClick( 
        (parent: AdapterView[_], view: View, position: Int, id: Long) => {
          convertAdapter(lv.getAdapter(), adapter => {
            val station = adapter.getItem(position)
            mainActorRef ! MainActor.RequestStation(station) 
          })
        }
      )  
      lv
    }

    Fragment.createListFragment(createHandler, createListView)

  }

  def createPlayer(slideLayout: LinearLayout with HorizontalSlideView): Fragment = {

    new Fragment() {

      override def onCreate(savedState: Bundle): Unit = {
        super.onCreate(savedState)
      }

      override def onCreateView(inflater: LayoutInflater, viewGroup: ViewGroup, savedState: Bundle): View = {

        val playerTextLayout = {
          val t = TextLayout.createTextLayout(getActivity(), "", "", "") 
          t.setBackgroundColor(TRANSPARENT)
          t.setLayoutParams(new RLLayoutParams(MATCH_PARENT, WRAP_CONTENT))
          t
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
          val layout = ImageSplitLayout.createMain(getActivity(), {
            val v = new View(getActivity()) 
            v.setBackgroundColor(GRAY)
            v
          })
          layout.setLayoutParams(new LLLayoutParams(width, WRAP_CONTENT))
          layout
        }

        val playerLayout =  {
          val l = ImageSplitLayout.createMain(getActivity(), playerProgressView)
          l.setLayoutParams(new LLLayoutParams(width, WRAP_CONTENT))
          l
        }

        val nextLayout =  {
          val l = ImageSplitLayout.createMain(getActivity(), {
            val v = new View(getActivity()) 
            v.setBackgroundColor(YELLOW)
            v
          })
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
                val trackIndex = adapter.getItemId(position)
                mainActorRef ! MainActor.ChangeTrackByIndex(trackIndex.toInt) 
              })
            }
          )
          lv
        }


        def stopProgress(): Unit = {
          frontBar.animate().cancel()
        }

        def animateProgress(duration: Int): Unit = {
          require(duration >= 0)

          val width = playerProgressView.getWidth()
          frontBar.animate()
            .x(width)
            .setDuration(duration)
            .setListener(new AnimatorListenerAdapter() {
              override def onAnimationEnd(animator: Animator): Unit = {
                frontBar.setX(0) 
              }
            })
        }

        var _trackDuration = -1
        var _playing = false 
        var _startPos = 0 

        def withAdapter(f: PlaylistAdapter => Unit): Unit = {
          convertAdapter(playlistView.getAdapter(), f)
        }

        val handler = new Handler(new HandlerCallback() {
          override def handleMessage(msg: Message): Boolean = {
            import UI._ 
            msg.obj match {
              case OnTrackIndexChanged(trackIndex) => 
                withAdapter(_.setTrackIndex(trackIndex))
                stopProgress()
                frontBar.setX(0)
                if (trackIndex < 0) {
                  _trackDuration = -1
                }
                true
              case OnTrackDurationChanged(duration) => 
                _trackDuration = duration
                if (_trackDuration >= 0 && _playing) {
                  animateProgress(_trackDuration)
                } else {
                  stopProgress()
                }
                true
              case OnLocalPlayingChanged(playing) =>
                Log.d("chakra", "OnLocalPlayingChanged: " + playing)
                _playing = playing
                if (_trackDuration >= 0 && _playing) {
                  animateProgress(_trackDuration)
                } else {
                  stopProgress()
                }
                true
              case OnLocalStartPosChanged(startPos) =>
                Log.d("chakra", "OnLocalStartPosChanged: " + startPos)
                _startPos = startPos 
                if (_trackDuration > -1) {
                  val width = playerProgressView.getWidth()
                  frontBar.setX(startPos * width/_trackDuration)
                }
                true
              case OnPlaylistChanged(playlist) => 
                withAdapter(_.setPlaylist(playlist))
                true
              case OnLocalTrackOptionChanged(trackOption) => 
                trackOption match {
                  case Some(track) =>
                    TextLayout.setTexts(playerTextLayout, track.title, track.artist, track.album)
                  case _ => 
                    TextLayout.setTexts(playerTextLayout, "", "", "")
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