package net.sourceforge.avalonirc.actors

import akka.actor.{Actor, ActorRef, Props}
import net.sourceforge.avalonirc.messages.{ConnectionOpened, MessageReceived}

import scala.collection.mutable.HashMap


class ServerActor extends Actor {

  val connections = new HashMap[Any, ActorRef]

  override def receive = {

    case ConnectionOpened( connection ) => {
      val newUser = context.actorOf(Props(new UserActor(connection)))
      connections.put(connection.id, newUser)
    }

    case MessageReceived( id, message ) => {
      connections.getOrElse(id, Actor.noSender) ! message
    }

  }
}
