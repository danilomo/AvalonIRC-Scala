package net.sourceforge.avalonirc.messages

import akka.actor.ActorRef
import net.sourceforge.avalonirc.server.{ClientConnection}

trait InternalMessage

final case class IsNickAvailable(nick: String) extends InternalMessage
final case class NickAvailable(nick: String) extends InternalMessage
final case class NickNotAvailable(nick: String) extends InternalMessage
final case class ConnectionOpened(connection: ClientConnection) extends InternalMessage
final case class MessageReceived(id: Any, message: UserMessage) extends InternalMessage
final case class RegisterNick(nick: String, ref: ActorRef) extends InternalMessage
final case class UnregisterNick( nick: String ) extends InternalMessage
final case class SendPrivateMsg( from: String, to: List[String], message: String) extends InternalMessage
final case class JoinChannelMsg( nick: String, fullName: String, channels: List[String], keys: List[String] )extends InternalMessage
final case class SendMsgToChannel( nick: String, senderName: String, channel: String, msg: String) extends InternalMessage
final case class RegisterUserInChannel(nick: String, fullName: String) extends InternalMessage
final case class JoinNotificationMsg(channelName: String, users: List[String]) extends InternalMessage
final case class UserListMessage(nick: String, channel: String, list: List[String]) extends InternalMessage
final case class ModeQuery(channel: String, nick: String) extends InternalMessage


final case class SendMessageToClient( from: String, message: String) extends InternalMessage {
  override def toString = from + " " + message
}