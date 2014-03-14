package com.feelchakra

import android.app.Fragment
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams._
import android.view._
import android.widget._


import android.util.Log 

import android.graphics.Color
import rx.lang.scala.Subject

import guava.scala.android.RichListView.listView2RichListView
import guava.scala.android.RichView.view2RichView

class PlayerFragment extends Fragment {

  private val that = this
  private val mainActorRef = MainActor.mainActorRef
  private var _playlistView: ListView = _
  private var _trackLayout: TrackLayout = _
  private var _adapter: PlaylistAdapter = _

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
    _trackLayout.setTrackOption(trackOption)
  }

  private def resizePlaylistView(playerOpen: Boolean): Unit = {

    if (playerOpen) {
      _playlistView.setLayoutParams {
        new LinearLayout.LayoutParams(MATCH_PARENT, 0, 6)
      }
    } else {
      _playlistView.setLayoutParams {
        new LinearLayout.LayoutParams(MATCH_PARENT, 0, 0)
      }
    }

  }

  private val handler = new Handler(new Handler.Callback() {
    override def handleMessage(msg: Message): Boolean = {
      import OutputHandler._ 
      msg.obj match {
        case OnTrackIndexChanged(trackIndex) => 
          that.setPlaylistCurrentTrack(trackIndex); true
        case OnPlaylistChanged(playlist) => 
          that.populatePlaylistView(playlist); true
        case OnTrackOptionChanged(trackOption) => 
          that.setTrackOption(trackOption); true
        case OnPlayerOpenChanged(playerOpen) => 
          that.resizePlaylistView(playerOpen); true
        case _ => false
      }
     false
    }
  })

  override def onCreate(savedState: Bundle): Unit = {
    super.onCreate(savedState)
  }

  override def onCreateView(inflater: LayoutInflater, viewGroup: ViewGroup, savedState: Bundle): View = {
    val verticalLayout = new LinearLayout(getActivity()) {
      setOrientation(LinearLayout.VERTICAL)

      addView {
        _trackLayout = new TrackLayout(getActivity)
        _trackLayout.setOnClick(
          view => { mainActorRef ! MainActor.FlipPlayer }
        )
        _trackLayout

      }

      addView {
        _playlistView = new ListView(getActivity()) {
          setBackgroundColor(Color.YELLOW)
          setLayoutParams(
            new LinearLayout.LayoutParams(MATCH_PARENT, 0, 0)
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
