package com.logan.feelchakra

import android.util.Log
import android.widget.Toast

class PlayerFragment extends Fragment {


  private val that = this
  private var _playlistView: ListView = _
  private var _playerView: ImageTextLayout = _
  private var _adapter: PlaylistAdapter = _

  import RichView.view2RichView
  import RichListView.listView2RichListView

  private def setPlaylistCurrentTrack(trackIndex: Int): Unit = {
    _playlistView.getAdapter() match {
      case adapter: PlaylistAdapter => {
        adapter.setTrackIndex(trackIndex)
      }
      case _ => {}
    } 
  }

  private def populatePlaylistView(playlist: List[Track]): Unit = {
    _playlistView.getAdapter() match {
      case adapter: PlaylistAdapter => {
        adapter.setPlaylist(playlist)
      }
      case _ => {}
    } 
  }

  private def setTrackOption(trackOption: Option[Track]): Unit = {
    trackOption match {
      case Some(track) =>
        _playerView.setTexts(track.title, track.artist, track.album)
      case _ => 
        _playerView.setTexts("", "", "")
    }
  }

  private val handler = new Handler(new HandlerCallback() {
    override def handleMessage(msg: Message): Boolean = {
      import UI._ 
      msg.obj match {
        case OnTrackIndexChanged(trackIndex) => 
          that.setPlaylistCurrentTrack(trackIndex); true
        case OnPlaylistChanged(playlist) => 
          that.populatePlaylistView(playlist); true
        case OnLocalTrackOptionChanged(trackOption) => 
          that.setTrackOption(trackOption); true
        case _ => false
      }
     false
    }
  })

  private def withMainActivity(f: MainActivity => Unit): Unit = {
    getActivity() match {
      case activity: MainActivity => {
        f(activity)
      }
      case _ => Log.d("chakra", "PlayerFragment: MainActivity missing")
    } 
  }

  override def onCreate(savedState: Bundle): Unit = {
    super.onCreate(savedState)
  }

  override def onCreateView(inflater: LayoutInflater, viewGroup: ViewGroup, savedState: Bundle): View = {
    val verticalLayout = new LinearLayout(getActivity()) {
      setOrientation(VERTICAL)

      addView {
        _playerView = new ImageTextLayout(getActivity, "", "", "", BLACK)

        /*
        _playerView.setOnClick(view => {
          mainActorRef ! MainActor.FlipPlayer
        })
        */

        _playerView

      }

      addView {
        _playlistView = new ListView(getActivity()) {
          setBackgroundColor(BLACK)
          setLayoutParams(
            new LLLayoutParams(MATCH_PARENT, MATCH_PARENT)
          ) 
        }
        val adapter = new PlaylistAdapter(getActivity())
        _playlistView.setAdapter(adapter)
        _playlistView.setOnItemClick( 
          (parent: AdapterView[_], view: View, position: Int, id: Long) => {
            val trackIndex = adapter.getItemId(position)
            mainActorRef ! MainActor.ChangeTrackByIndex(trackIndex.toInt) 
          }
        ); _playlistView 
      }

    }

    mainActorRef ! MainActor.Subscribe(this.toString, handler) 
    verticalLayout
  }

  override def onDestroy(): Unit =  {
    super.onDestroy()
    mainActorRef ! MainActor.Unsubscribe(this.toString)
  }

}
