package net.sourceforge.avalonirc.server

import scala.concurrent.Future

trait ClientConnection {
  def write( message: String ): Future[WriteOperation]
  def id: Any
}

case class WriteOperation(success: Boolean, canceled: Boolean)