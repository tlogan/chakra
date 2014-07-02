package com.logan.feelchakra

trait StationListAdapter {
  this: BaseAdapter =>

  override def getItem(position: Int): Station
  def setStationList(stationList: List[Station]): Unit
  def setStationConnection(stationConnection: StationConnection): Unit

}

object StationListAdapter {

  def create(context: Context): BaseAdapter with StationListAdapter = {
    var _stationList: List[Station] = List() 
    var _stationConnection: StationConnection = StationDisconnected 

    new BaseAdapter() with StationListAdapter {

      override def getCount(): Int = _stationList.size
      override def getItem(position: Int): Station = _stationList(getItemId(position).toInt)
      override def getItemId(position: Int): Long = position 
      override def getView(position: Int, view: View, viewGroup: ViewGroup): View = {

        val station = getItem(position)

        new LinearLayout(context) {
          setOrientation(VERTICAL)
          _stationConnection match {
            case StationConnected(connectedStation) if station == connectedStation => 
              setBackgroundColor(BLUE)
            case StationRequested(reqStation) if station == reqStation => 
              setBackgroundColor(YELLOW)
            case _ =>
              setBackgroundColor(GRAY)
          }
          List(station.device.deviceName, station.domain, "") foreach {
            (text: String) => { 
              addView {
                new TextView(context) {
                  setText(text)
                  setTextColor(WHITE)
                }
              }
            } 
          }
        }


      }

      override def setStationList(stationList: List[Station]): Unit = {
        _stationList = stationList
        this.notifyDataSetChanged()
      }

      override def setStationConnection(stationConnection: StationConnection): Unit = {
        _stationConnection = stationConnection
        this.notifyDataSetChanged()
      }

    }
  }
}
