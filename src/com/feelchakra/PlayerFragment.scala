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

object PlayerFragment {

  case class OnMainActorConnected(trackIndex: Int, playlist: List[Track]) 
  case class OnPlayListChanged(trackIndex: Int, playlist: List[Track]) 
  case class OnPlayerFlipped(playerOpen: Boolean) 

}

class PlayerFragment extends Fragment {

  private val that = this
  private val mainActorRef = MainActor.mainActorRef
  private var _listView: ListView = _
  private var _trackLayout: TrackLayout = _
  private var _adapter: PlaylistAdapter = _

  private val handler = new Handler(new Handler.Callback() {
    override def handleMessage(msg: Message): Boolean = {
      import PlayerFragment._
      msg.obj match {
        case OnMainActorConnected(trackOption, playlist) => 
          that.onMainActorConnected(trackOption, playlist); true
        case OnPlayListChanged(trackIndex, playlist) => 
          that.onPlaylistChanged(trackIndex, playlist); true
        case OnPlayerFlipped(playerOpen) => 
          that.onPlayerFlipped(playerOpen); true
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
        _trackLayout.setOnClick {
          view => { mainActorRef ! MainActor.FlipPlayer }
        }
        _trackLayout

      }

      addView {
        _listView = new ListView(getActivity()) {
          setBackgroundColor(Color.YELLOW)
          setLayoutParams {
            new LinearLayout.LayoutParams(MATCH_PARENT, 0, 0)
          }
        }; _listView
      }

    }

    mainActorRef ! MainActor.SetPlayerFragmentHandler(handler) 
    verticalLayout
  }

  private def onMainActorConnected(trackIndex: Int, playlist: List[Track]): Unit = {

    _trackLayout.onTrackOptionChanged(playlist.lift(trackIndex))

    //update the track listview
    _listView setAdapter {
      new PlaylistAdapter(getActivity(), trackIndex, playlist)
    } 

    _listView setOnItemClick { 
      (parent: AdapterView[_], view: View, position: Int, id: Long) => {
        val track = _listView.getAdapter() match {
          case adapter: PlaylistAdapter => adapter.getItem(position)
        } 
        mainActorRef ! MainActor.SetTrack(track) 
      }
    }  

  }


  private def onPlaylistChanged(trackIndex: Int, playlist: List[Track]): Unit = {

    _listView.getAdapter() match {
      case adapter: PlaylistAdapter => {
        adapter.onPlaylistChanged(trackIndex, playlist)
      }
      case _ => {}
    } 

    _trackLayout.onTrackOptionChanged(playlist.lift(trackIndex))


  }

  private def onPlayerFlipped(playerOpen: Boolean): Unit = {

    if (playerOpen) {
      _listView.setLayoutParams {
        new LinearLayout.LayoutParams(MATCH_PARENT, 0, 6)
      }
    } else {
      _listView.setLayoutParams {
        new LinearLayout.LayoutParams(MATCH_PARENT, 0, 0)
      }
    }

  }

}
