package net.sourceforge.avalonirc.server

import java.net.InetAddress

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import akka.actor.ActorSystem
import io.netty.handler.ssl.SslContext

class IRCServer(val port: Int, val system: ActorSystem) {


  def start() = {

    val sslCtx: SslContext = null

    val bossGroup = new NioEventLoopGroup(1)
    val workerGroup = new NioEventLoopGroup

    try {
      val b = new ServerBootstrap

      b.group(bossGroup, workerGroup)
        .channel(classOf[NioServerSocketChannel])
        .handler(new LoggingHandler(LogLevel.INFO))
        .childHandler(new IRCChannelInitializer( new IRCServerHandler(system) ))

      b.bind(port)
        .sync
        .channel
        .closeFuture
        .sync

    } finally {
      bossGroup.shutdownGracefully()
      workerGroup.shutdownGracefully()
    }
  }
}


object IRCServer{
  val HOST_NAME = InetAddress.getLocalHost.getHostName

  def welcomeMessage( nick: String ) = s":$HOST_NAME 001 $nick :Welcome to the Internet Relay Network, $nick!\r\n"
}
