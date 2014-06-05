package com.logan.feelchakra

import RichListView.listView2RichListView
import RichContext.context2RichContext
import android.util.Log
import android.widget.Toast

class AlbumSelectionFragment extends Fragment {

  private val that = this
  private var _verticalLayout: LinearLayout = _
  private var _listView: ListView = _
  private var _adapter: TrackListAdapter = _

  private val handler = new Handler(new HandlerCallback() {
    override def handleMessage(msg: Message): Boolean = {
      import UI._

      msg.obj match {
        case OnAlbumMapChanged(albumMap) => 
          setAlbumMap(albumMap)
          true
        case OnAlbumTupleOpChanged(albumTupleOp) =>
          setAlbumTupleOp(albumTupleOp)
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
          val adapter = new AlbumListAdapter(getActivity())
          this.setAdapter(adapter) 
          setDivider(new ColorDrawable(BLACK))
          setDividerHeight(getActivity().dp(6))
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



  private def withAdapter(f: AlbumListAdapter => Unit): Unit = {
    _listView.getAdapter() match {
      case adapter: AlbumListAdapter => {
        f(adapter)
      }
      case _ => Log.d("chakra", "ArtistListAdapter missing")
    } 
  }

  private def setAlbumMap(albumMap: AlbumMap): Unit = {
    withAdapter(adapter => {
      adapter.setAlbumMap(albumMap)
    })
  }

  private def setAlbumTupleOp(albumTupleOp: Option[(String, List[Track])]): Unit = {
    withAdapter(adapter => {
      adapter.setAlbumTupleOp(albumTupleOp)
      if (albumTupleOp != None) {
        val pos = adapter.albumTuplePosition
        _listView.setSelectionFromTop(pos, 0)
      }
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
