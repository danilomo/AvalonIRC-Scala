package net.sourceforge.avalonirc.server.netty

import akka.actor.ActorSystem
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import net.sourceforge.avalonirc.messages.{ConnectionOpened, InvalidMessage, MessageReceived, UserMessage}

@Sharable
class IRCServerHandler(val system: ActorSystem) extends SimpleChannelInboundHandler[String] {

  val serverActor = system.actorSelection("akka://AvalonIRC/user/server" )

  override def handlerAdded(context: ChannelHandlerContext): Unit = {
    serverActor ! new ConnectionOpened( new NettyConnection(context) )
  }

  override def channelRead0(ctx: ChannelHandlerContext, msg: String): Unit = {

    val message = UserMessage(msg)
    val id  = ctx.channel().id()

    message match {
      case _: InvalidMessage => {
        // TODO Response with proper message
      }

      case _ => {
        serverActor ! MessageReceived( id, message )
      }
    }
  }

  override def channelReadComplete(ctx: ChannelHandlerContext): Unit = {
    ctx.flush
  }

}
