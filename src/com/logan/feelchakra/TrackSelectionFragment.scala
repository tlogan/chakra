package com.logan.feelchakra

import RichListView.listView2RichListView
import RichContext.context2RichContext
import android.util.Log
import android.widget.Toast

object TrackSelectionFragment {

  def create(): Fragment = {

    def convertAdapter(adapter: ListAdapter, f: BaseAdapter with TrackListAdapter => Unit): Unit = {
      adapter match {
        case adapter: BaseAdapter with TrackListAdapter => {
          f(adapter)
        }
      } 
    }

    def createHandler(listView: ListView) = {

      new Handler(new HandlerCallback() {
        override def handleMessage(msg: Message): Boolean = {

          val adapter = listView.getAdapter()
          def withAdapter(f: TrackListAdapter => Unit): Unit = {
            convertAdapter(adapter, f)
          }

          import UI._
          msg.obj match {
            case OnTrackListChanged(trackList) => 
              withAdapter(_.setTrackList(trackList))
              true
            case OnPresentTrackOptionChanged(trackOption) => 
              withAdapter(_.setPresentTrackOption(trackOption))
              true
            case OnFutureTrackListChanged(list) => 
              withAdapter(_.setFutureTrackMap(trackMap(list)))
              true

            case _ => false
          }
        }
      })

    }

    def createListView(context: Context): ListView = {
      val lv = ListView.createMain(context, TrackListAdapter.create(context))
      lv.setOnItemClick( 
        (parent: AdapterView[_], view: View, position: Int, id: Long) => {
          convertAdapter(lv.getAdapter(), adapter => {
            val track =  adapter.getItem(position)
            mainActorRef ! MainActor.AppendOrRemoveFutureTrack(track) 
          })
        }
      ) 
      lv 
    }

    ListFragment.create(createHandler, createListView)

  }

}
