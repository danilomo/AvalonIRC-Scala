package net.sourceforge.avalonirc

import akka.actor.{ActorSystem, Props}
import net.sourceforge.avalonirc.actors.{ChannelsActor, ServerActor, UsersActor}
import net.sourceforge.avalonirc.server.netty.IRCServer

/**
 * @author ${user.name}
 */
object ServerMain extends App {

  val system = ActorSystem("AvalonIRC")

  val usersActor = system.actorOf(Props[UsersActor], "users")
  val serverActor = system.actorOf(Props[ServerActor], "server")
  val channelsActor = system.actorOf(Props[ChannelsActor], "channels")

  val ircServer = new IRCServer(6667, system )

  ircServer.start()

}
