package com.logan.feelchakra

import RichListView.listView2RichListView
import RichContext.context2RichContext
import android.util.Log
import android.widget.Toast

object ArtistSelectionFragment {

  def create(): Fragment = {

    def createHandler(listView: ListView) = {
      new Handler(new HandlerCallback() {
        override def handleMessage(msg: Message): Boolean = {

          val adapter = listView.getAdapter()
          def withAdapter(f: ArtistListAdapter => Unit): Unit = {
            adapter match {
              case adapter: ArtistListAdapter => {
                f(adapter)
              }
            } 
          }

          import UI._
          msg.obj match {
            case OnArtistMapChanged(artistMap) => 
              withAdapter(_.setArtistMap(artistMap))
              true
            case OnArtistTupleOpChanged(artistTupleOp) =>
              withAdapter(artistAdapter => {
                artistAdapter.setArtistTupleOp(artistTupleOp)
                if (artistTupleOp != None) {
                  val pos = artistAdapter.artistTuplePosition
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

    def createListView(context: Context): ListView = { 
      val lv =  ListView.createMain(context, ArtistListAdapter.create(context))
      lv.setLayoutParams(new LLLayoutParams(MATCH_PARENT, MATCH_PARENT))
      lv
    }

    ListFragment.create(createHandler, createListView)

  }

}
