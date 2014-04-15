package com.logan.feelchakra

import android.util.Log

class NetworkProfile(
  val serviceName: String,
  val serviceType: String,
  val localAddressOp: Option[InetSocketAddress]
) { 

  def this() = this(
    "_chakra",
    "_syncstream._tcp",
    None
  )

  import MainActor._
  import UI._

  def setLocalAddress(localAddress: InetSocketAddress) : NetworkProfile = {
    val newProfile = new NetworkProfile(serviceName, serviceType, Some(localAddress))
    mainActorRef ! NotifyHandlers(OnProfileChanged(newProfile))
    newProfile
  }

}
  
