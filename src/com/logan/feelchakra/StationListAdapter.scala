package com.logan.feelchakra

trait StationListAdapter {
  this: BaseAdapter =>

  override def getItem(position: Int): Station
  def setStationList(stationList: List[Station]): Unit
}

object StationListAdapter {

  def create(context: Context): BaseAdapter with StationListAdapter = {
    var _stationList: List[Station] = List() 

    new BaseAdapter() with StationListAdapter {

      override def getCount(): Int = _stationList.size
      override def getItem(position: Int): Station = _stationList(getItemId(position).toInt)
      override def getItemId(position: Int): Long = position 
      override def getView(position: Int, view: View, viewGroup: ViewGroup): View = {

        val station = getItem(position)

        new LinearLayout(context) {
          setOrientation(VERTICAL)
          setBackgroundColor(GRAY)
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

    }
  }
}
