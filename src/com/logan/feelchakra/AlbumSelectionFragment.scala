package com.logan.feelchakra

import RichListView.listView2RichListView
import android.util.Log
import android.widget.Toast

class AlbumSelectionFragment extends Fragment {

  private val that = this
  private var _verticalLayout: LinearLayout = _
  private var _listView: ListView = _
  private var _adapter: TrackListAdapter = _
  private var selectedPosition: Int = 0

  private val handler = new Handler(new HandlerCallback() {
    override def handleMessage(msg: Message): Boolean = {
      import UI._

      msg.obj match {
        case OnAlbumMapChanged(albumMap) => 
          setAlbumMap(albumMap)
          true
        case OnAlbumTupleOpChanged(albumTupleOp) =>
          albumTupleOp match {
            case Some(albumTuple) => 
              setAlbumTuple(albumTuple)
            case None => //closeAlbumList
          }
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
          this.setOnItemClick( 
            (parent: AdapterView[_], view: View, position: Int, id: Long) => {
              selectedPosition = position
              val albumTuple =  adapter.getItem(position)
              mainActorRef ! MainActor.SetAlbumTuple(albumTuple) 
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
      adapter.setAlbumList(albumMap.toList)
    })
  }

  private def setAlbumTuple(albumTuple: (String, List[Track])): Unit = {
    withAdapter(adapter => {
      adapter.setAlbumTuple(albumTuple)
      _listView.setSelectionFromTop(selectedPosition, 0)
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
