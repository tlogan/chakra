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

class TrackSelectionFragment extends Fragment {

  private val that = this
  private val mainActorRef = MainActor.mainActorRef
  private var _verticalLayout: LinearLayout = _
  private var _listView: ListView = _
  private var _adapter: TrackListAdapter = _

  private val handler = new Handler(new Handler.Callback() {
    override def handleMessage(msg: Message): Boolean = {
      import OutputHandler._
      msg.obj match {
        case OnTrackListChanged(trackList) => 
          that.populateListView(trackList); true
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

    mainActorRef ! MainActor.Subscribe(this.toString, handler) 
    _verticalLayout
  }

  override def onDestroy(): Unit =  {
    super.onDestroy()
    mainActorRef ! MainActor.Unsubscribe(this.toString)
  }

  private def populateListView(trackList: List[Track]): Unit = {
    
    _listView.getAdapter() match {
      case adapter: TrackListAdapter => {
        adapter.setTrackList(trackList)
      }
      case _ => {
        val adapter = new TrackListAdapter(getActivity(), trackList)
        _listView.setAdapter(adapter) 
        _listView.setOnItemClick( 
          (parent: AdapterView[_], view: View, position: Int, id: Long) => {
            val track =  adapter.getItem(position)
            mainActorRef ! MainActor.AddTrackToPlaylist(track) 
          }
        ) 
      }
    } 

  }


}
