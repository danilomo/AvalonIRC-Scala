package net.sourceforge.avalonirc.server

import akka.actor.ActorSystem
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import net.sourceforge.avalonirc.messages.{OpenConnection, InvalidMessage, MessageReceived, UserMessage}

@Sharable
class IRCServerHandler(val system: ActorSystem) extends SimpleChannelInboundHandler[String] {

  val serverActor = system.actorSelection("akka://AvalonIRC/user/server" )

  override def handlerAdded(ctx: ChannelHandlerContext): Unit = {
    serverActor ! new OpenConnection( ctx.channel().id(), ctx )
  }

  override def channelRead0(ctx: ChannelHandlerContext, msg: String): Unit = {

    val message = UserMessage(msg)
    val id  = ctx.channel().id()
    println("<<<" + msg.trim())

    message match {
      case _: InvalidMessage => {
        //println("Invalid message " + msg)
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
