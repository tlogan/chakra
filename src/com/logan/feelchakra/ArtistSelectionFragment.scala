package com.logan.feelchakra

import RichListView.listView2RichListView
import android.util.Log
import android.widget.Toast

class ArtistSelectionFragment extends Fragment {

  private val that = this
  private var _verticalLayout: LinearLayout = _
  private var _listView: ListView = _
  private var _adapter: TrackListAdapter = _
  private var selectedPosition: Int = 0

  private val handler = new Handler(new HandlerCallback() {
    override def handleMessage(msg: Message): Boolean = {
      import UI._
      msg.obj match {
        case OnArtistMapChanged(artistMap) => 
          populateListView(artistMap)
          true
        case OnArtistTupleOpChanged(artistTupleOp) =>
          artistTupleOp match {
            case Some(artistTuple) => 
              setArtistTuple(artistTuple)
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

      setLayoutParams(new VGLayoutParams(MATCH_PARENT, WRAP_CONTENT))

      setOrientation(VERTICAL)
      addView {
        _listView = new ListView(getActivity())
        _listView.setLayoutParams(new LLLayoutParams(MATCH_PARENT, WRAP_CONTENT))
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

  private def populateListView(artistMap: ArtistMap): Unit = {
    _listView.getAdapter() match {
      case adapter: ArtistListAdapter => {
        adapter.setArtistList(artistMap.toList)
      }
      case _ => {
        val adapter = new ArtistListAdapter(getActivity(), artistMap.toList)
        _listView.setAdapter(adapter) 
        _listView.setOnItemClick( 
          (parent: AdapterView[_], view: View, position: Int, id: Long) => {
            selectedPosition = position
            val artistTuple =  adapter.getItem(position)
            mainActorRef ! MainActor.SetArtistTuple(artistTuple) 

            //Toast.makeText(getActivity(), "artistTuple: " + artistTuple, Toast.LENGTH_SHORT).show()
            Log.d("chakra", "artistTuple: " + artistTuple)
          }
        ) 
      }
    } 

  }

  private def doOnAdapter(f: ArtistListAdapter => Unit): Unit = {
    _listView.getAdapter() match {
      case adapter: ArtistListAdapter => {
        f(adapter)
      }
      case _ => Log.d("chakra", "ArtistListAdapter missing")
    } 
  }

  private def setArtistTuple(artistTuple: (String, AlbumMap)): Unit = {
    doOnAdapter(adapter => {
      adapter.setArtistTuple(artistTuple)
      _listView.setSelectionFromTop(selectedPosition, 0)
    })
  }

  private def setPlaylist(playlist: List[Track]): Unit = {
    doOnAdapter(adapter => {
      adapter.setPlaymap(Playmap(playlist))
    })
  }


  private def setTrackOption(trackOption: Option[Track]): Unit = {
    doOnAdapter(adapter => {
      adapter.setTrackOption(trackOption)
    })
  }

}
