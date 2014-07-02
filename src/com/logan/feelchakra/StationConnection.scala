package com.logan.feelchakra

import android.util.Log

sealed trait StationConnection 
case object StationDisconnected extends StationConnection
case class StationRequested(station: Station) extends StationConnection
case class StationConnected(station: Station) extends StationConnection
