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

object TrackSelectionFragment {
  case class OnMainActorConnected(trackList: List[Track])
  case class OnTrackListChanged(trackList: List[Track])
}

class TrackSelectionFragment extends Fragment {

  private val that = this
  private val mainActorRef = MainActor.mainActorRef
  private var _verticalLayout: LinearLayout = _
  private var _listView: ListView = _
  private var _adapter: TrackListAdapter = _

  private val handler = new Handler(new Handler.Callback() {
    override def handleMessage(msg: Message): Boolean = {
      import TrackSelectionFragment._
      msg.obj match {
        case OnMainActorConnected(trackList) => 
          that.onMainActorConnected(trackList); true
        case OnTrackListChanged(trackList) => 
          that.onTrackListChanged(trackList); true
        case _ => false
      }
    }
  })

  override def onCreate(savedState: Bundle): Unit = {
    super.onCreate(savedState)
  }

  override def onCreateView(inflater: LayoutInflater, viewGroup: ViewGroup, savedState: Bundle): View = {
    _verticalLayout = new LinearLayout(getActivity()) {
      setOrientation(LinearLayout.VERTICAL)
      addView {
        _listView = new ListView(getActivity()) {
        }; _listView
      }

    }

    mainActorRef ! MainActor.SetTrackSelectionFragmentHandler(handler) 
    _verticalLayout
  }

  private def onMainActorConnected(trackList: List[Track]): Unit = {

    _listView setAdapter {
      new TrackListAdapter(getActivity(), trackList)
    } 

    _listView setOnItemClick { 
      (parent: AdapterView[_], view: View, position: Int, id: Long) => {
        val track = _listView.getAdapter() match {
          case adapter: TrackListAdapter => adapter.getItem(position)
        } 
        mainActorRef ! MainActor.AddTrackToPlaylist(track) 
        Log.d("trackSelectionFrag", "setting track: " + track.title)
      }
    }  

  }

  private def onTrackListChanged(trackList: List[Track]): Unit = {

    _listView.getAdapter() match {
      case adapter: TrackListAdapter => adapter.setTrackList(trackList)
    } 

  }


}
