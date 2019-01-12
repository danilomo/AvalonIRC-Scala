package net.sourceforge.avalonirc.actors

import akka.actor.{Actor, ActorRef, Props}
import io.netty.channel.ChannelId
import net.sourceforge.avalonirc.messages.{OpenConnection, MessageReceived}

import scala.collection.mutable.HashMap


class ServerActor extends Actor {

  val connections = new HashMap[ChannelId, ActorRef]

  override def receive = {

    case OpenConnection( id, channel ) => {
      val newUser = context.actorOf(Props(new UserActor(id, channel)), name = id.asShortText() )
      connections.put(id, newUser)
    }

    case MessageReceived( id, message ) => {
      connections.getOrElse(id, Actor.noSender) ! message
    }

  }
}
