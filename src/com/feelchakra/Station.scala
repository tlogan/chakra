package com.feelchakra

import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager._

case class Station(domain: String, record: java.util.Map[String, String], device: WifiP2pDevice)
