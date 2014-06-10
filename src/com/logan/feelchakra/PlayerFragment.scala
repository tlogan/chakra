package com.logan.feelchakra

import android.util.Log
import android.widget.Toast

import RichView.view2RichView
import RichListView.listView2RichListView
import RichContext.context2RichContext

class PlayerFragment extends Fragment {

  private val that = this

  private lazy val adapter: PlaylistAdapter = new PlaylistAdapter(getActivity())

  private lazy val playlistView: ListView = new MainListView(getActivity(), adapter) {

    setLayoutParams(
      new LLLayoutParams(MATCH_PARENT, MATCH_PARENT)
    ) 
    this.setOnItemClick( 
      (parent: AdapterView[_], view: View, position: Int, id: Long) => {
        withAdapter(adapter => {
          val trackIndex = adapter.getItemId(position)
          mainActorRef ! MainActor.ChangeTrackByIndex(trackIndex.toInt) 
        })
      }
    )
  }

  private def withAdapter(f: PlaylistAdapter => Unit): Unit = {
    playlistView.getAdapter() match {
      case adapter: PlaylistAdapter => {
        f(adapter)
      }
      case _ => Log.d("chakra", "PlaylistAdapter missing")
    } 
  }

  private lazy val playerTextLayout: TextLayout = new TextLayout(getActivity(), "", "", "") {
    setBackgroundColor(TRANSPARENT)
    setLayoutParams(new RLLayoutParams(MATCH_PARENT, WRAP_CONTENT))
  }

  private lazy val backBar = new View(getActivity()) {
    setBackgroundColor(BLUE)
    setLayoutParams(new RLLayoutParams(MATCH_PARENT, MATCH_PARENT))
  }

  private lazy val frontBar = new View(getActivity()) {
    setBackgroundColor(DKBLUE)
    setLayoutParams(new RLLayoutParams(MATCH_PARENT, MATCH_PARENT))
  }

  private lazy val playerProgressView = new RelativeLayout(getActivity()) {
    addView(backBar)
    addView(frontBar)
    addView(playerTextLayout)
  }

  private lazy val playerLayout: ImageSplitLayout = new MainImageSplitLayout(getActivity(), playerProgressView) {
    setPadding(0, getActivity().dp(16), 0, getActivity().dp(16))
  }

  private lazy val verticalLayout = new LinearLayout(getActivity()) {
    setOrientation(VERTICAL)
    addView(playerLayout)
    addView(playlistView)
  }


  private def setPlaylistCurrentTrack(trackIndex: Int): Unit = {
    playlistView.getAdapter() match {
      case adapter: PlaylistAdapter => {
        adapter.setTrackIndex(trackIndex)
      }
      case _ => {}
    } 
  }

  private def populatePlaylistView(playlist: List[Track]): Unit = {
    playlistView.getAdapter() match {
      case adapter: PlaylistAdapter => {
        adapter.setPlaylist(playlist)
      }
      case _ => {}
    } 
  }

  private def setTrackOption(trackOption: Option[Track]): Unit = {
    trackOption match {
      case Some(track) =>
        playerTextLayout.setTexts(track.title, track.artist, track.album)
      case _ => 
        playerTextLayout.setTexts("", "", "")
    }
  }

  private var _trackDuration = -1
  private var _playing = false 
  private var _startPos = 0 

  private val handler = new Handler(new HandlerCallback() {
    override def handleMessage(msg: Message): Boolean = {
      import UI._ 
      msg.obj match {
        case OnTrackIndexChanged(trackIndex) => 
          that.setPlaylistCurrentTrack(trackIndex)
          true
        case OnTrackDurationChanged(duration) => 
          _trackDuration = duration
          if (_trackDuration >= 0 && _playing) {
            animateProgress(_trackDuration)
          } else stopProgress()
          true
        case OnLocalPlayingChanged(playing) =>
          Log.d("chakra", "OnLocalPlayingChanged: " + playing)
          _playing = playing
          if (_trackDuration >= 0 && _playing) {
            animateProgress(_trackDuration)
          } else stopProgress()
          true
        case OnLocalStartPosChanged(startPos) =>
          Log.d("chakra", "OnLocalStartPosChanged: " + startPos)
          _startPos = startPos 
          true
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
    mainActorRef ! MainActor.Subscribe(this.toString, handler) 
    verticalLayout
  }

  override def onDestroy(): Unit =  {
    super.onDestroy()
    mainActorRef ! MainActor.Unsubscribe(this.toString)
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

}
