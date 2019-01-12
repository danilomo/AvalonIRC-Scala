package net.sourceforge.avalonirc.actors

import akka.actor.{Actor, ActorRef, Props}

import scala.collection.mutable.HashMap
import net.sourceforge.avalonirc.messages._

class ChannelsActor extends Actor {

  val users = context.system.actorSelection("akka://AvalonIRC/user/users")
  val channels = new HashMap[String, ActorRef]()

  override def receive: Receive = {
    case JoinChannelMsg(nick, fullName, chlist, keys) => {
      joinUser(nick, fullName, chlist, keys)
    }

    case s: SendMsgToChannel => {
      channels.getOrElse(s.channel, context.system.deadLetters) forward s
    }

    case q: ModeQuery => {
      channels.getOrElse(q.channel, context.system.deadLetters) forward q
    }
  }

  def joinUser(nick: String, fullName:String, chlist: List[String], keys: List[String]): Unit = {
    (chlist zip keys) foreach ( tp => {
      val (ch, key) = tp

      if(channels.contains(ch)){
        registerUserInChannel(nick, fullName, ch, key)
      }else{
        createNewChannel(nick, fullName, ch)
      }

    })
  }

  def registerUserInChannel(nick: String, fullName: String, ch: String, key: String): Unit = {
    (channels get ch) match {
      case Some(channel) => {
        channel ! new RegisterUserInChannel(nick, fullName)
      }
      case None => {}
    }
  }

  def createNewChannel(nick: String, fullName: String, ch: String): Unit = {
    val channel = context.actorOf(Props(new ChannelActor(ch)))
    channels.put(ch, channel)
    channel ! new RegisterUserInChannel(nick, fullName)
  }

}
