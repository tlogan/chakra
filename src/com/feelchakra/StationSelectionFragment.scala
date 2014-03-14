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
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager._
import android.net.wifi.p2p.nsd.WifiP2pServiceRequest
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest

import android.widget.Toast

import android.util.Log 

import android.graphics.Color
import rx.lang.scala.Subject

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter

import guava.scala.android.RichListView.listView2RichListView
import scala.collection.JavaConversions._ 


class StationSelectionFragment extends Fragment {

  private val that = this
  private val mainActorRef = MainActor.mainActorRef
  private var _verticalLayout: LinearLayout = _
  private var _listView: ListView = _
  private var _adapter: StationListAdapter = _


  private val handler = new Handler(new Handler.Callback() {
    override def handleMessage(msg: Message): Boolean = {
      import OutputHandler._
      msg.obj match {
        case OnStationListChanged(stationList) => 
          that.populateListView(stationList); true
        case OnStationOptionChanged(stationOption) => 
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
      setOrientation(LinearLayout.VERTICAL)
      addView {
        _listView = new ListView(getActivity()) {
        }; _listView
      }
    }

    mainActorRef ! MainActor.Subscribe(this.toString, handler) 

    _verticalLayout
  }



  override def onResume() {
    super.onResume()
  }

  override def onPause() {
    super.onPause()
  }

  override def onStop() {
    super.onStop()
  }

  override def onDestroy(): Unit =  {
    super.onDestroy()
    mainActorRef ! MainActor.Unsubscribe(this.toString)
  }


  private def populateListView(stationList: List[Station]): Unit = {

    _listView.getAdapter() match {
      case adapter: StationListAdapter => {
        adapter.onStationListChanged(stationList)
      }
      case _ => {
        val adapter = new StationListAdapter(getActivity(), stationList)
        _listView.setAdapter(adapter) 

        _listView.setOnItemClick( 
          (parent: AdapterView[_], view: View, position: Int, id: Long) => {
            val station = adapter.getItem(position)
            mainActorRef ! MainActor.ConnectToStation(station) 
          }
        )  
      }
    } 

  }




}
