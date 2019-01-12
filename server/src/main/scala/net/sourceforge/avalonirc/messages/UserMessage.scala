package net.sourceforge.avalonirc.messages

sealed trait UserMessage

final case class Nick(nickName: String, hopCount: Int = 0) extends UserMessage
final case class User(userName: String, hostName: String, serverName: String, realName: String) extends UserMessage
final case class Password(password: String) extends UserMessage
final case class PrivateMessage(receivers: List[String], message: String) extends UserMessage
final case class MessageToChannel(channel: String, message: String) extends UserMessage
final case class Join(channels: List[String], keys: List[String] = Nil) extends UserMessage
final case class Quit(quitMsg: Option[String] = None) extends UserMessage
final case class InvalidMessage() extends UserMessage
final case class Ping(server: String) extends UserMessage
final case class Mode(channel: String, mode: Option[String]) extends UserMessage

case object InvalidMessage {
  private val instance = new InvalidMessage()

  def apply(): UserMessage = instance
}

object UserMessage {
  def parseNickMsg(args: List[String]): UserMessage = args match {
    case nick :: Nil => new Nick(nick)
    case nick :: hop :: _
      if hop matches """\d+""" =>
      new Nick(nick, hop.toInt)
    case _ => InvalidMessage()
  }

  def parseUserMsg(args: List[String]): UserMessage = args match {
    case user :: host :: server :: real => new User(user, host, server, real.mkString(" ").substring(1))
    case _ => InvalidMessage()
  }

  def parsePrivateMsg(args: List[String]): UserMessage = args match {
    case senders :: message :: Nil => if (senders.startsWith("#"))
      new MessageToChannel(senders, message.substring(1))
    else
      new PrivateMessage(senders.split(",").toList, message.substring(1))
    case _ => InvalidMessage()
  }

  def parseJoinMsg(args: List[String]): UserMessage = args match {
    case channels :: Nil => {
      val channelList = channels.split(",").toList
      val keys = ((1 to channelList.size) map (x => "")).toList
      new Join(channelList, keys)
    }

    case channels :: keys :: _ => {
      val channelList = channels.split(",").toList
      val keyList = keys.split(",").toList
      val diff = channelList.size - keyList.size

      if(diff == 0)
        new Join(channelList, keyList)
      else if(diff > 0){
        val newKeyList = keyList ++ ((1 to diff) map (x => "")).toList
        new Join(channelList, newKeyList)
      }else{
        InvalidMessage()
      }
    }

    case _ => InvalidMessage()
  }

  def parseModeMsg(args: List[String]): UserMessage = args match {
    case channel :: Nil => new Mode(channel, None)
    case channel :: mode :: _ => new Mode(channel, Some(mode))
    case _ => InvalidMessage()
  }

  def splitCommand(message: String, elements: Int): List[String] = {
    def splitCommandAux(message: String, index: Int, list: List[String]): List[String] = {
      if (index < elements - 1) {
        val pos = message.indexOf(" ")
        if (pos > 0)
          splitCommandAux(message.substring(pos + 1), index + 1, message.substring(0, pos) :: list)
        else
          message :: list
      } else
        message :: list
    }

    splitCommandAux(message, 0, Nil).reverse
  }

  def apply(msg: String): UserMessage = {
    val message = msg.trim()
    val pos = message.indexOf(" ")

    val head = if (pos > 0) message.substring(0, pos).toUpperCase else message
    val body = if (pos > 0) message.substring(pos + 1) else ""

    head.toString match {
      case "NICK" => parseNickMsg(splitCommand(body, 2))
      case "USER" => parseUserMsg(splitCommand(body, 4))
      case "PASS" => new Password(body)
      case "PRIVMSG" => parsePrivateMsg(splitCommand(body, 2))
      case "QUIT" => if (body.trim.isEmpty) new Quit(None) else new Quit(Some(body))
      case "JOIN" => parseJoinMsg(splitCommand(body, 2))
      case "PING" => new Ping(body.trim())
      case "MODE" => parseModeMsg(splitCommand(body, 2))
      case _ => InvalidMessage()
    }

  }
}