package com.logan.feelchakra

import RichListView.listView2RichListView
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
          populateListView(albumMap)
          true
        case OnAlbumTupleOpChanged(albumTupleOp) =>
          albumTupleOp match {
            case Some(albumTuple) => 
              openTrackList(albumTuple)
            case None => //closeAlbumList
          }
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

  private def populateListView(albumMap: AlbumMap): Unit = {
    _listView.getAdapter() match {
      case adapter: AlbumListAdapter => {
        adapter.setAlbumList(albumMap.toList)
      }
      case _ => {
        val adapter = new AlbumListAdapter(getActivity(), albumMap.toList)
        _listView.setAdapter(adapter) 
        _listView.setOnItemClick( 
          (parent: AdapterView[_], view: View, position: Int, id: Long) => {
            val albumTuple =  adapter.getItem(position)
            mainActorRef ! MainActor.SetAlbumTuple(albumTuple) 
          }
        ) 
      }
    } 

  }

  private def openTrackList(albumTuple: (String, List[Track])): Unit = {
    _listView.getAdapter() match {
      case adapter: AlbumListAdapter => {
        adapter.setAlbumTuple(albumTuple)
      }
      case _ => Log.d("chakra", "ArtistListAdapter missing")
    } 
  }


}
