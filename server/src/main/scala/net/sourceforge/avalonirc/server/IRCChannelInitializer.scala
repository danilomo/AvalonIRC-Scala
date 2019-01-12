package net.sourceforge.avalonirc.server

import java.util

import io.netty.channel.{ChannelHandlerContext, ChannelInitializer}
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.{DelimiterBasedFrameDecoder, Delimiters}
import io.netty.handler.codec.string.{StringDecoder, StringEncoder}

class IRCChannelInitializer(val ircHandler: IRCServerHandler) extends ChannelInitializer[SocketChannel]{
  private val encoder = new StringDecoder
  private val decoder = new StringEncoder

  override def initChannel(ch: SocketChannel) = {
    val pipeline = ch.pipeline

    pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter().head ))
    pipeline.addLast(encoder)
    pipeline.addLast(decoder)

    pipeline.addLast(new StringEncoder(){
      override def encode(ctx: ChannelHandlerContext, msg: CharSequence, out: util.List[AnyRef]): Unit = {
        println(">>>" + msg)
        super.encode(ctx, msg, out)
      }
    })

    pipeline.addLast(ircHandler)


  }
}
