package net.sourceforge.avalonirc.actors

import akka.actor.{Actor, ActorRef, Props}

import scala.collection.mutable.ArrayBuffer
import net.sourceforge.avalonirc.messages._
import net.sourceforge.avalonirc.server.IRCServer

class ChannelActor(name: String) extends Actor {

  val usersList = ArrayBuffer[String]()

  val users = context.system.actorSelection("akka://AvalonIRC/user/users")

  //val bots = List( context.actorOf(Props(new ChatBot(self))) )
  val bots: List[ActorRef] = List()

  var topic: Option[String] = None

  override def receive: Receive = {
    case RegisterUserInChannel(nick, fullName) => {
      usersList append nick

      users ! new SendPrivateMsg(":" + fullName, usersList.toList, "JOIN " + name)
      users ! new SendPrivateMsg(":" + IRCServer.HOST_NAME, usersList.toList, s"331 $nick $name :No topic is set")
      users ! new UserListMessage(nick, name, usersList.toList)
    }

    case ModeQuery(_, nick) => {
      sender() ! new SendMessageToClient(":" + IRCServer.HOST_NAME, s"324 $nick $name +" )
    }

    case SendMsgToChannel(nick, from,  _, message) => {
      val list = (usersList filter (! _.equals(nick) )).toList
      val msg = new SendPrivateMsg(":" + from, list,s"PRIVMSG $name :$message")
      users ! msg

      bots.foreach(
        ref => ref ! message.toString
      )
    }

    case s: String => {
      val list = usersList toList
      val from = "chatbot@" + IRCServer.HOST_NAME
      val msg = new SendPrivateMsg(":" + from, list,s"PRIVMSG $name :$s")

      users ! msg
    }
  }

}

object ChannelActor{
  val MAX_SIZE = 20
}



class ChatBot(val chatRoom: ActorRef) extends Actor{

  override def receive: Receive = {
    case s: String => {
        val response: String = s.toUpperCase()
        chatRoom ! response
    }
  }


}

