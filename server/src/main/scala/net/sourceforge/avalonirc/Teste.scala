package net.sourceforge.avalonirc

import akka.actor.ActorSystem
import net.sourceforge.avalonirc.actors.ChannelUserList

object Teste extends App {

  import scala.collection.JavaConverters._

  val system = ActorSystem("zzz")

  val x = system.settings.config.getStringList(
    "zzz.akka.avionics.flightcrew.attendantNames").asScala.toList

  println(x)

}