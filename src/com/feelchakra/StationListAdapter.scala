package com.feelchakra

import scala.collection.immutable.List

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams._
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import android.widget._
import android.graphics.Color

import rx.lang.scala.Observer

class StationListAdapter(activity: Activity, initialStationList: List[Station]) extends BaseAdapter {

  private var _stationList: List[Station] = initialStationList

  override def getCount(): Int = _stationList.size

  override def getItem(position: Int): Station = _stationList(getItemId(position).toInt)

  override def getItemId(position: Int): Long = position 

  override def getView(position: Int, view: View, viewGroup: ViewGroup): View = {

    val station = getItem(position)

    new LinearLayout(activity) {
      setOrientation(LinearLayout.VERTICAL)
      setBackgroundColor(Color.GRAY)
      List(station.domain, "", "") foreach {
        (text: String) => { 
          addView {
            new TextView(activity) {
              setText(text)
              setTextColor(Color.WHITE)
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
