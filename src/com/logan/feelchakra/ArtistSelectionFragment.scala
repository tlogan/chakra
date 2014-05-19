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
          setArtistMap(artistMap)
          true
        case OnArtistTupleOpChanged(artistTupleOp) =>
          setArtistTupleOp(artistTupleOp)
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
        val adapter = new ArtistListAdapter(getActivity())
        _listView.setAdapter(adapter) 
        _listView.setOnItemClick( 
          (parent: AdapterView[_], view: View, position: Int, id: Long) => {
            selectedPosition = position
            val artistTuple =  adapter.getItem(position)
            mainActorRef ! MainActor.SelectArtistTuple(artistTuple) 

            //Toast.makeText(getActivity(), "artistTuple: " + artistTuple, Toast.LENGTH_SHORT).show()
            Log.d("chakra", "artistTuple: " + artistTuple)
          }
        ) 
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

  private def withAdapter(f: ArtistListAdapter => Unit): Unit = {
    _listView.getAdapter() match {
      case adapter: ArtistListAdapter => {
        f(adapter)
      }
      case _ => Log.d("chakra", "ArtistListAdapter missing")
    } 
  }

  private def setArtistMap(artistMap: ArtistMap): Unit = {
    withAdapter(adapter => {
      adapter.setArtistList(artistMap.toList)
    })
  }

  private def setArtistTupleOp(artistTupleOp: Option[(String, AlbumMap)]): Unit = {
    withAdapter(adapter => {
      adapter.setArtistTupleOp(artistTupleOp)
      if (artistTupleOp != None) {
        _listView.setSelectionFromTop(selectedPosition, 0)
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
