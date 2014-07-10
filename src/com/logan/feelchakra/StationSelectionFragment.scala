package com.logan.feelchakra

import RichListView.listView2RichListView
import RichContext.context2RichContext
import android.util.Log
import android.widget.Toast

object StationSelectionFragment {

  def create(): Fragment = {

    def convertAdapter(adapter: ListAdapter, f: StationListAdapter => Unit): Unit = {
      adapter match {
        case adapter: StationListAdapter => {
          f(adapter)
        }
      } 
    }

    def createHandler(listView: ListView) = {

      val adapter = listView.getAdapter() 

      def withAdapter(f: StationListAdapter => Unit): Unit = {
        convertAdapter(adapter, f)
      }

      new Handler(new HandlerCallback() {
        override def handleMessage(msg: Message): Boolean = {
          import UI._
          msg.obj match {
            case OnStationListChanged(stationList) => 
              withAdapter(_.setStationList(stationList))
              true
            case OnStationConnectionChanged(stationConnection) => 
              withAdapter(_.setStationConnection(stationConnection))
              true
            case _ => false
          }
        }
      })
    }

    def createListView(context: Context): ListView = {
      val lv = ListView.createMain(context, StationListAdapter.create(context))
      lv.setOnItemClick( 
        (parent: AdapterView[_], view: View, position: Int, id: Long) => {
          convertAdapter(lv.getAdapter(), adapter => {
            val station = adapter.getItem(position)
            mainActorRef ! MainActor.RequestStation(station) 
          })
        }
      )  
      lv
    }

    ListFragment.create(createHandler, createListView)

  }
}
