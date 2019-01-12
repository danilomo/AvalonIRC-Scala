package net.sourceforge.avalonirc.actors

import akka.actor.{Actor, ActorRef}
import net.sourceforge.avalonirc.messages._

import scala.collection.mutable.HashMap


class UsersActor extends Actor {

  val nicks = new HashMap[String, ActorRef]()

  override def receive = {

    case IsNickAvailable(nick) => {
      sender() ! (if (nicks.contains(nick))
        NickNotAvailable(nick)
      else
        NickAvailable(nick))
    }

    case RegisterNick(nick, ref) => {
      nicks.put(nick, ref)
    }

    case UnregisterNick(nick) => {
      nicks.remove(nick)
    }

    case SendPrivateMsg(from, receivers, message) => {
      val list = (receivers collect nicks) zip receivers

      list.foreach(
        x => {
          x._1 ! new SendMessageToClient(from, message)
        })

    }

    case msg: UserListMessage => {
      nicks.getOrElse(msg.nick, context.system.deadLetters) forward msg
    }
  }

}