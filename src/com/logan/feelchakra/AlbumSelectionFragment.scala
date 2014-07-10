package com.logan.feelchakra

import RichListView.listView2RichListView
import RichContext.context2RichContext
import android.util.Log
import android.widget.Toast

object AlbumSelectionFragment {

  def create(): Fragment = {

    def createHandler(listView: ListView) = {
      new Handler(new HandlerCallback() {
        override def handleMessage(msg: Message): Boolean = {

          val adapter = listView.getAdapter()
          def withAdapter(f: AlbumListAdapter => Unit): Unit = {
            adapter match {
              case adapter: AlbumListAdapter => {
                f(adapter)
              }
            } 
          }

          import UI._
          msg.obj match {
            case OnAlbumMapChanged(albumMap) => 
              withAdapter(_.setAlbumMap(albumMap))
              true
            case OnAlbumTupleOpChanged(albumTupleOp) =>
              withAdapter(adapter => {
                adapter.setAlbumTupleOp(albumTupleOp)
                if (albumTupleOp != None) {
                  val pos = adapter.albumTuplePosition
                  listView.setSelectionFromTop(pos, 0)
                }
              })
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

    def createListView(context: Context): ListView = ListView.createMain(context, AlbumListAdapter.create(context))

    ListFragment.create(createHandler, createListView)

  }

}
