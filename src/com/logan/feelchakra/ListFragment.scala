package com.logan.feelchakra

import RichListView.listView2RichListView
import RichContext.context2RichContext
import android.util.Log
import android.widget.Toast

object ListFragment {

  def create(createHandler: ListView => Handler, createListView: Context => ListView): Fragment = {

    new Fragment() {

      override def onCreate(savedState: Bundle): Unit = {
        super.onCreate(savedState)
      }

      override def onCreateView(inflater: LayoutInflater, viewGroup: ViewGroup, savedState: Bundle): View = {

        val listView = createListView(this.getActivity())
        mainActorRef ! MainActor.Subscribe(this.toString, createHandler(listView)) 


        val layout = new LinearLayout(this.getActivity())
        layout.setOrientation(VERTICAL)
        layout.addView(listView)

        val handlerTwo = new Handler(new HandlerCallback() {
          override def handleMessage(msg: Message): Boolean = {

            import UI._
            msg.obj match {
              case OnModHeightChanged(height) => 
                listView.setLayoutParams(new LLLayoutParams(MATCH_PARENT, height))
                true
              case _ => false
            }
          }
        })

        mainActorRef ! MainActor.Subscribe(this.toString + "2", handlerTwo) 

        layout

      }

      override def onDestroy(): Unit =  {
        super.onDestroy()
        mainActorRef ! MainActor.Unsubscribe(this.toString)
        mainActorRef ! MainActor.Unsubscribe(this.toString + "2")
      }


    }

  }





}
