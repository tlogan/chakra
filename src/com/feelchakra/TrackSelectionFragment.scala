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

import android.graphics.Color

import guava.android.RichListView.listView2RichListView

object TrackSelectionFragment {
   val mainActorConnected = 1;
}

class TrackSelectionFragment extends Fragment {

  private val that = this
  private val mainActorRef = MainActor.mainActorRef
  private var _verticalLayout: LinearLayout = _
  private var _listView: ListView = _
  private var _adapter: TrackListAdapter = _

  private val handler = new Handler(new Handler.Callback() {
    override def handleMessage(msg: Message): Boolean = {
      msg.obj match {
        case trackList: List[Track] if (msg.what == TrackSelectionFragment.mainActorConnected) => 
          that.onMainActorConnected(trackList); true
        case _ => false
      }
    }
  })

  override def onCreate(savedState: Bundle): Unit = {
    super.onCreate(savedState)
  }

  override def onCreateView(inflater: LayoutInflater, viewGroup: ViewGroup, savedState: Bundle): View = {
    _verticalLayout = new LinearLayout(getActivity()) {
      addView {
        _listView = new ListView(getActivity()) {
          setBackgroundColor(Color.GREEN)
          setLayoutParams {
            new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
          }
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
        Toast.makeText(getActivity(), "track " + track.title + " chosen", Toast.LENGTH_SHORT).show()
      }
    }  

  }

}
