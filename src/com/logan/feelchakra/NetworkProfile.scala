package com.logan.feelchakra

import android.util.Log

case class NetworkProfile(
  serviceName: String,
  serviceType: String,
  localAddressOp: Option[InetSocketAddress]
) { 

  def this() = this(
    "_chakra",
    "_syncstream._tcp",
    None
  )

  import MainActor._
  import UI._

  def setLocalAddress(localAddress: InetSocketAddress) : NetworkProfile = {
    val newProfile = this.copy(localAddressOp = Some(localAddress))
    mainActorRef ! NotifyHandlers(OnProfileChanged(newProfile))
    newProfile
  }

}
  
