package net.sourceforge.avalonirc.actors

import akka.actor.Actor
import net.sourceforge.avalonirc.messages._
import net.sourceforge.avalonirc.server.{ClientConnection, ReplyCodes}
import net.sourceforge.avalonirc.server.netty.IRCServer
import scala.concurrent.ExecutionContext

class UserActor(val connection: ClientConnection) extends Actor {

  implicit val ec = ExecutionContext.global

  val users = context.system.actorSelection("akka://AvalonIRC/user/users")
  var channels = context.system.actorSelection("akka://AvalonIRC/user/channels")

  var nick: Option[String] = None
  var user: Option[String] = None
  var host: Option[String] = None
  var fullName: Option[String] = None

  def nameAtHost() = for {
    usr <- user;
    hst <- host;
    nick <- nick
  } yield nick + "!" + usr + "@" + hst

  def checkIfAuthenticated(): Unit = {

    val nickAndUser = for {
      n <- nick;
      u <- user
    } yield (n, u)

    nickAndUser match {
      case Some((nick, _)) => {
        users ! RegisterNick(nick, self)
        context.become(receiveWhenRegistered)
        connection.write(IRCServer.welcomeMessage(nick))
      }
      case _ => {}
    }
  }

  override def receive: Receive = receiveWhenNotRegistered

  def receiveWhenNotRegistered: Receive = {

    case Nick(nick, _) => {
      users ! new IsNickAvailable(nick)
    }

    case User(userName, hName, _, _) => {
      //TODO - Authentication
      user = Some(userName)
      host = Some(hName)
      checkIfAuthenticated()
    }

    case NickAvailable(n) => {
      nick = Some(n)
      checkIfAuthenticated()
    }

    case NickNotAvailable(nick) => {
      val host = IRCServer.HOST_NAME
      val code = ReplyCodes.ERR_ALREADYREGISTRED
      connection.write(s":$host $code * $nick :Nickname already in use\r\n")
    }

  }

  def receiveWhenRegistered: Receive = {

    case PrivateMessage(receivers, message) => {
      val body = "PRIVMSG " + nick.get + " :" + message
      users ! new SendPrivateMsg(nameAtHost().get, receivers, body)
    }

    case MessageToChannel(channel, message) => {
      channels ! new SendMsgToChannel(nick.get, nameAtHost().get, channel, message)
    }

    case Join(chs, keys) => {
      channels ! new JoinChannelMsg(nick.get, nameAtHost().get, chs, keys)
    }

    case msg: SendMessageToClient => {
      connection.write(msg.toString() + "\r\n")
    }

    case msg: UserListMessage => {
      val c = new ChannelUserList(self, msg.list,
        msg.channel, msg.nick, IRCServer.HOST_NAME)

      self ! c
    }

    case c: ChannelUserList => {
      val (more, line) = c.nextMessage()

      if (more) {
        connection.write(line + "\r\n").onComplete(
          _ => {
            self ! c
          }
        )
      } else
        connection.write(line + "\r\n")
    }

    case Ping(server) => {
      connection.write(":" + IRCServer.HOST_NAME + " PONG " + IRCServer.HOST_NAME + " " + server + "\r\n")
    }

    case Mode(channel, None) => {
      channels ! new ModeQuery(channel, nick.get)
    }

    case _: Quit => {
      users ! new UnregisterNick(nick.get)
      val future = connection.write("Bye bye!!\r\n")
      future.onComplete( _ => connection.close() )
    }
  }
}