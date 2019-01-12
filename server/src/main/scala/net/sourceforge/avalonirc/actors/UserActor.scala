package net.sourceforge.avalonirc.actors

import akka.actor.Actor
import io.netty.channel.{ChannelFuture, ChannelFutureListener, ChannelHandlerContext, ChannelId}
import net.sourceforge.avalonirc.messages._
import net.sourceforge.avalonirc.server.{IRCServer, ReplyCodes}

class UserActor(val channelId: ChannelId,
                val channelContext: ChannelHandlerContext) extends Actor {

  val channel = channelContext.channel()
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
      case Some((nick, user)) => {
        channel.writeAndFlush(IRCServer.welcomeMessage(nick))
        users ! RegisterNick(nick, self)
        context.become(receiveRegistered)
      }
      case _ => {}
    }
  }

  override def receive: Receive = {

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
      channel.writeAndFlush(s":$host $code * $nick :Nickname already in use\r\n")
    }

  }

  def receiveRegistered: Receive = {
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
      channel.writeAndFlush(msg.toString() + "\r\n")
    }

    case msg: UserListMessage => {
      val c = new ChannelUserList(self, msg.list,
        msg.channel, msg.nick, IRCServer.HOST_NAME)

      self ! c
    }

    case c: ChannelUserList => {
      val (more, line) = c.nextMessage()

      if (more) {
        channel.writeAndFlush(line + "\r\n").addListener(
          (fut: ChannelFuture) => {
            self ! c
          }
        )
      } else
        channel.writeAndFlush(line + "\r\n")
    }

    case Ping(server) => {
      println(":" + IRCServer.HOST_NAME + " PONG " + IRCServer.HOST_NAME + " " + server + "\r\n")
      channel.writeAndFlush(":" + IRCServer.HOST_NAME + " PONG " + IRCServer.HOST_NAME + " " + server + "\r\n")
    }

    case Mode(channel, None) => {
      channels ! new ModeQuery(channel, nick.get)
    }

    case _: Quit => {
      users ! new UnregisterNick(nick.get)
      val fut = channel.writeAndFlush("Bye bye!\r\n")
      fut.addListener(ChannelFutureListener.CLOSE)
    }
  }
}