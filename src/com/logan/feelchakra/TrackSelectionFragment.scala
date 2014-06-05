package com.logan.feelchakra

import RichListView.listView2RichListView
import RichContext.context2RichContext
import android.util.Log
import android.widget.Toast

class TrackSelectionFragment extends Fragment {

  private val that = this
  private var _verticalLayout: LinearLayout = _
  private var _listView: ListView = _
  private var _adapter: TrackListAdapter = _

  private val handler = new Handler(new HandlerCallback() {
    override def handleMessage(msg: Message): Boolean = {
      import UI._
      msg.obj match {
        case OnTrackListChanged(trackList) => 
          that.setTrackList(trackList)
          true
        case OnPlaylistChanged(playlist) => 
          that.setPlaylist(playlist)
          true
        case OnLocalTrackOptionChanged(trackOption) => 
          that.setTrackOption(trackOption)
          true
        case _ => false
      }
    }
  })

  override def onCreate(savedState: Bundle): Unit = {
    super.onCreate(savedState)
  }

  override def onCreateView(inflater: LayoutInflater, viewGroup: ViewGroup, savedState: Bundle): View = {
    _verticalLayout = new LinearLayout(getActivity()) {
      setOrientation(VERTICAL)
      addView {
        _listView = new ListView(getActivity()) {
          val adapter = new TrackListAdapter(getActivity())
          this.setAdapter(adapter) 
          setDivider(new ColorDrawable(BLACK))
          setDividerHeight(getActivity().dp(6))
          this.setOnItemClick( 
            (parent: AdapterView[_], view: View, position: Int, id: Long) => {
              val track =  adapter.getItem(position)
              mainActorRef ! MainActor.AddPlaylistTrack(track) 
            }
          ) 
        }
        _listView
      }

    }

    mainActorRef ! MainActor.Subscribe(this.toString, handler) 
    _verticalLayout
  }

  override def onDestroy(): Unit =  {
    super.onDestroy()
    mainActorRef ! MainActor.Unsubscribe(this.toString)
  }

  private def withAdapter(f: TrackListAdapter => Unit): Unit = {
    _listView.getAdapter() match {
      case adapter: TrackListAdapter => {
        f(adapter)
      }
      case _ => Log.d("chakra", "ArtistListAdapter missing")
    } 
  }

  private def setTrackList(trackList: List[Track]): Unit = {
    withAdapter(adapter => {
      adapter.setTrackList(trackList)
    })
  }

  private def setPlaylist(playlist: List[Track]): Unit = {
    withAdapter(adapter => {
      adapter.setPlaymap(Playmap(playlist))
    })
  }


  private def setTrackOption(trackOption: Option[Track]): Unit = {
    withAdapter(adapter => {
      adapter.setTrackOption(trackOption)
    })
  }


}
