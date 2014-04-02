package com.logan.feelchakra

class StationListAdapter(activity: Activity, initialStationList: List[Station]) extends BaseAdapter {

  private var _stationList: List[Station] = initialStationList

  override def getCount(): Int = _stationList.size

  override def getItem(position: Int): Station = _stationList(getItemId(position).toInt)

  override def getItemId(position: Int): Long = position 

  override def getView(position: Int, view: View, viewGroup: ViewGroup): View = {

    val station = getItem(position)

    new LinearLayout(activity) {
      setOrientation(VERTICAL)
      setBackgroundColor(GRAY)
      List(station.device.deviceName, station.domain, "") foreach {
        (text: String) => { 
          addView {
            new TextView(activity) {
              setText(text)
              setTextColor(WHITE)
            }
          }
        } 
      }
    }


  }

  def onStationListChanged(stationList: List[Station]): Unit = {
    _stationList = stationList
    this.notifyDataSetChanged()
  }

}
