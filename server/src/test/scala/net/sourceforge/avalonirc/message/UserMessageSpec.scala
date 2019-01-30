package net.sourceforge.avalonirc.message

import org.scalatest.{Matchers, WordSpec}
import net.sourceforge.avalonirc.messages._

class UserMessageSpec extends WordSpec with Matchers{

  "The UserMessage singleton" should {

    "parse a NICK message" in {
      val nick = "bob"
      val string = s"NICK $nick"
      val message = UserMessage(string)
      message shouldBe new Nick(nick)
    }

    "parse a USR message" in {
      val username = "bob"
      val servername = "server"
      val hostname = "host"
      val realname = "Bob Collins"
      val string = s"USER $username $hostname $servername :$realname"
      val message = UserMessage(string)
      message shouldBe new User(username, hostname, servername, realname)
    }

    "parse a PWD message" in {
      val password = "123455563453"
      val string = s"PASS $password"
      val message = UserMessage(string)
      message shouldBe Password(password)
    }

    "produce an InvalidMessage object from a malformed input" in {
      val message = UserMessage("sdafjsdaflçasjdfasdf")
      message shouldBe InvalidMessage()
    }

  }

}
