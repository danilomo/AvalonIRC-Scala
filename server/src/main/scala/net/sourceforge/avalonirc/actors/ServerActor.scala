package net.sourceforge.avalonirc.actors

import akka.actor.{Actor, ActorRef, Props}
import net.sourceforge.avalonirc.messages.{ConnectionOpened, MessageReceived}
import net.sourceforge.avalonirc.server.ConnectionId

import scala.collection.mutable.HashMap


class ServerActor extends Actor {

  val connections = new HashMap[Any, ActorRef]

  override def receive = {

    case ConnectionOpened( connection ) => {

      println("[1]")
      println(connection.id)
      println(connection.id.toString)

      val newUser = context.actorOf(Props(new UserActor(connection)))
      connections.put(connection.id, newUser)
      println(connections.get(connection.id))
      println("")
    }

    case MessageReceived( id, message ) => {
      println("[2]")
      println(">conn " + connections)
      println(">id " + id)
      println(">message " + message)
      println(">conn.get " + connections.get(id))
      connections.getOrElse(id, Actor.noSender) ! message
    }

  }
}
