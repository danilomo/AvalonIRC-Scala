package net.sourceforge.avalonirc.server

import akka.actor.ActorSystem

trait Server {
  def start(): Unit
  def actorSystem(): ActorSystem
}

class FakeServer(val actorSystem: ActorSystem) extends Server{
  override def start(): Unit = {}

  def openConnection(): ClientConnection = ???
}
