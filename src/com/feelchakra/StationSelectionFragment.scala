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

import android.util.Log 

import android.graphics.Color
import rx.lang.scala.Subject

import guava.scala.android.RichListView.listView2RichListView
import scala.collection.JavaConversions._ 

object StationSelectionFragment {
  case class OnMainActorConnected(stationList: List[Station])
  case class OnStationListChanged(stationList: List[Station])
}

class StationSelectionFragment extends Fragment {

  private val that = this
  private val mainActorRef = MainActor.mainActorRef
  private var _verticalLayout: LinearLayout = _
  private var _listView: ListView = _
  private var _adapter: StationListAdapter = _

  private var _manager: WifiP2pManager = _
  private var _channel: WifiP2pManager.Channel = _

  private val handler = new Handler(new Handler.Callback() {
    override def handleMessage(msg: Message): Boolean = {
      import StationSelectionFragment._
      msg.obj match {
        case OnMainActorConnected(stationList) => 
          that.onMainActorConnected(stationList); true
        case OnStationListChanged(stationList) => 
          that.onStationListChanged(stationList); true
        case _ => false
      }
    }
  })

  override def onCreate(savedState: Bundle): Unit = {
    super.onCreate(savedState)


    _manager = getActivity().getSystemService(Context.WIFI_P2P_SERVICE) match {
      case m: WifiP2pManager => m
    }
    _channel = _manager.initialize(getActivity(), getActivity().getMainLooper(), null)

    val serviceListener = new DnsSdServiceResponseListener() {
      override def onDnsSdServiceAvailable(name: String, regType: String, device: WifiP2pDevice): Unit = {
        mainActorRef ! MainActor.CommitStation(device)
      }
    }

    val recordListener = new DnsSdTxtRecordListener() {
      override def onDnsSdTxtRecordAvailable(domain: String, record: java.util.Map[String, String], 
        device: WifiP2pDevice
      ): Unit = {
        val station = Station(domain, record, device)
        mainActorRef ! MainActor.AddStation(station)
      }
    }

    _manager.setDnsSdResponseListeners(_channel, serviceListener, recordListener)


  }

  override def onCreateView(inflater: LayoutInflater, viewGroup: ViewGroup, savedState: Bundle): View = {
    _verticalLayout = new LinearLayout(getActivity()) {
      setOrientation(LinearLayout.VERTICAL)
      addView {
        _listView = new ListView(getActivity()) {
        }; _listView
      }
    }

    val serviceRequest = WifiP2pDnsSdServiceRequest.newInstance()
    _manager.addServiceRequest(_channel, serviceRequest, new ActionListener() {
      override def onSuccess(): Unit = {}
      override def onFailure(code: Int): Unit = {}
    })
    _manager.discoverServices(_channel, new ActionListener() {
      override def onSuccess(): Unit = {}
      override def onFailure(code: Int): Unit = {}
    })


   

    mainActorRef ! MainActor.SetStationSelectionFragmentHandler(handler) 
    _verticalLayout
  }

  private def onMainActorConnected(stationList: List[Station]): Unit = {

    _listView setAdapter {
      new StationListAdapter(getActivity(), stationList)
    } 

    _listView setOnItemClick { 
      (parent: AdapterView[_], view: View, position: Int, id: Long) => {
        val station = _listView.getAdapter() match {
          case adapter: StationListAdapter => adapter.getItem(position)
        } 
        //mainActorRef ! MainActor.SetStation(station) 
        Log.d("stationSelectionFrag", "setting station list: " + stationList.size)
      }
    }  

  }

  private def onStationListChanged(stationList: List[Station]): Unit = {

    _listView.getAdapter() match {
      case adapter: StationListAdapter => adapter.onStationListChanged(stationList)
    } 

  }


}
