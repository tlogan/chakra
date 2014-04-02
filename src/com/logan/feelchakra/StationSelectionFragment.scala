package com.logan.feelchakra

class StationSelectionFragment extends Fragment {

  private val that = this
  private val mainActorRef = MainActor.mainActorRef
  private var _verticalLayout: LinearLayout = _
  private var _listView: ListView = _
  private var _adapter: StationListAdapter = _

  import RichListView.listView2RichListView

  private val handler = new Handler(new HandlerCallback() {
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
      setOrientation(VERTICAL)
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
