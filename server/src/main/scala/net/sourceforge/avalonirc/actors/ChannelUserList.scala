package net.sourceforge.avalonirc.actors

import akka.actor.ActorRef

class ChannelUserList(val userActor: ActorRef,
                       val users: List[String], val channel: String,
                      val user:String, val serverName: String) {

  private val prefix = ":" + serverName + " 353 = " + channel + " :"
  private var buffer = new StringBuilder(prefix)
  private var list = users
  private var end = false

  // we assume a char equals to 2 bytes
  private def getBuff( list: List[String],
                       buffer: StringBuilder,
                       size: Int): (List[String], StringBuilder) = {
    list match{
      case Nil => ( Nil, buffer )

      case head :: tail => {
        if(buffer.size + head.length > size)
          (list, buffer)
        else{
          buffer.append(head).append(" ")
          getBuff(tail, buffer, size)
        }
      }
    }
  }

  def nextMessage(): (Boolean, String) = {

    if(this.end){
      return (false, ":" + serverName + " 366 = " + channel + " :End of NAMES list")
    }

    val tuple = getBuff(list, buffer, 50)

    tuple match {
      case (Nil, buf) => {
        end = true
        (true, buf.toString())
      }

      case (list, buf) => {
        this.list = list
        this.buffer = new StringBuilder(prefix)

        (true, buf.toString())
      }
    }

  }

}
