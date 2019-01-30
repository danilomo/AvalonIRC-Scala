package net.sourceforge.avalonirc.actors

import akka.actor.{ActorSystem, Props}
import net.sourceforge.avalonirc.server.FakeServer
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import net.sourceforge.avalonirc.messages._


class ServerActorSpec extends WordSpec
  with BeforeAndAfterAll
  with Matchers{

  lazy val system = ActorSystem("AvalonIRC")

  "The server actor " should {

    "register a user after a pair of nick, user messages" in {
      val server = new FakeServer(system)
      val conn1 = server.openConnection
      conn1.sendMessage(Nick("rona"))
      conn1.sendMessage(User("rona", "", "", ""))
      val response = conn1.response()
      assert(response.contains(":Welcome to the Internet Relay Network, rona!"))

      val conn2 = server.openConnection
      conn2.sendMessage(User("ronaldo", "", "", ""))
      conn2.sendMessage(Nick("ronaldo"))
      val response2 = conn2.response()
      assert(response2.contains(":Welcome to the Internet Relay Network, ronaldo!"))
    }

    "prohibit the registration of a nick already registered" in {
      val server = new FakeServer(system)
      val conn1 = server.openConnection
      conn1.sendMessage(Nick("amadeus"))
      conn1.sendMessage(User("amadeus", "", "", ""))
      conn1.response()

      val conn2 = server.openConnection
      conn2.sendMessage(User("amadeus", "", "", ""))
      conn2.sendMessage(Nick("amadeus"))
      val response2 = conn2.response()
      assert(response2.contains("Nickname already in use"))
    }

    "exchange messages between users" in {
      val server = new FakeServer(system)
      val conn1 = server.openConnection
      conn1.sendMessage(Nick("user1"))
      conn1.sendMessage(User("user1", "", "", ""))
      conn1.response()

      val conn2 = server.openConnection
      conn2.sendMessage(User("user2", "", "", ""))
      conn2.sendMessage(Nick("user2"))
      conn2.response()

      conn1.sendMessage(PrivateMessage(List("user2"), "Alowwwww"))

      val response = conn2.response()
      assert("user1!user1@ PRIVMSG user1 :Alowwwww" == response.trim)
    }

  }

  override def beforeAll(): Unit = {
    system.actorOf(Props[UsersActor], "users")
    system.actorOf(Props[ServerActor], "server")
    system.actorOf(Props[ChannelsActor], "channels")
  }

  override def afterAll(): Unit = {
    system.terminate()
  }


}
