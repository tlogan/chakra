package com.logan.feelchakra

import RichListView.listView2RichListView
import android.util.Log
import android.widget.Toast

class ArtistSelectionFragment extends Fragment {

  private val that = this
  private var _verticalLayout: LinearLayout = _
  private var _listView: ListView = _
  private var _adapter: TrackListAdapter = _

  private val handler = new Handler(new HandlerCallback() {
    override def handleMessage(msg: Message): Boolean = {
      import UI._
      msg.obj match {
        case OnArtistMapChanged(artistMap) => 
          populateListView(artistMap)
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
        }; _listView
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
            val artistTuple =  adapter.getItem(position)

            Toast.makeText(getActivity(), "artistTuple: " + artistTuple, Toast.LENGTH_SHORT).show()
            Log.d("chakra", "artistTuple: " + artistTuple)
            //mainActorRef ! MainActor.AddTrackToPlaylist(track) 
          }
        ) 
      }
    } 

  }


}
