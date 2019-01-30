package net.sourceforge.avalonirc.server

import akka.actor.{ActorSelection, ActorSystem}
import java.util.concurrent.atomic.AtomicInteger
import net.sourceforge.avalonirc.messages.{ConnectionOpened, MessageReceived, UserMessage}
import scala.concurrent.{Future, Promise}
import java.util.concurrent.{BlockingQueue, LinkedBlockingQueue}

trait Server {
  def start(): Unit
  def actorSystem(): ActorSystem
}

class FakeServer(val actorSystem: ActorSystem) extends Server{

  val connectionsCounter = new AtomicInteger(0)
  val serverActor = actorSystem.actorSelection("akka://AvalonIRC/user/server" )

  override def start(): Unit = {}

  def openConnection(): FakeConnection = {
    val connection = new FakeConnection(connectionsCounter.incrementAndGet(), serverActor)
    serverActor !  new ConnectionOpened(connection)
    connection
  }

}

class FakeConnection(val id: Int, serverActor: ActorSelection) extends ClientConnection {
  var closed = false
  val buffer = new LinkedBlockingQueue[String]()

  override def write( message: String ): Future[WriteOperation] = {
    if(closed){
      throw new RuntimeException("Trying to write on closed connection")
    }

    buffer.add(message)

    val promise = Promise[WriteOperation]()
    promise success new WriteOperation(true, true )
    promise.future
  }

  def sendMessage(message: UserMessage) = {
    serverActor ! MessageReceived( id, message )
  }

  def response(): String = buffer.take()

  def close() = {
    closed = true
  }
}