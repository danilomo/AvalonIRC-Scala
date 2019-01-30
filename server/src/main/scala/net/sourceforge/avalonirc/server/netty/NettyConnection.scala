package net.sourceforge.avalonirc.server.netty

import io.netty.channel.{ChannelFuture, ChannelHandlerContext}
import net.sourceforge.avalonirc.server.{ClientConnection, WriteOperation}

import scala.concurrent.Promise

class NettyConnection(context: ChannelHandlerContext) extends ClientConnection {

  private  val channel = context.channel()
  //override val id      = context.channel.id()

  override def write(message: String) = {
    val nettyFuture = channel.writeAndFlush(message)
    val promise = Promise[WriteOperation]()

    nettyFuture.addListener(
      (future: ChannelFuture) => {
        val result = new WriteOperation(
          future.isDone && future.isSuccess,
          future.isDone && future.isCancelled
        )

        promise.success(result)
      }
    )

    promise.future
  }
  //override def id = new ConnectionId(context.channel().id())
  override def id = context.channel().id
}
