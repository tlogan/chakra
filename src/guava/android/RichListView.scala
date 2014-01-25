package guava.android 

import android.widget.ListView
import android.widget.AdapterView
import android.view.View

object RichListView {

  implicit def listView2RichListView(listView: ListView): RichListView = new RichListView(listView)

}

class RichListView(listView: ListView) {

  def setOnItemClick(f: (AdapterView[_], View, Int, Long) => Unit): Unit = {

    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      override def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long) = {
        f(parent, view, position, id)
      }
    })

  }

}
