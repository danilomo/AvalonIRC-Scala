package net.sourceforge.avalonirc.server

class ConnectionId( wrappedId: Any ) {

  override def equals( obj: Any) = wrappedId.equals(obj)

  override def hashCode(): Int = wrappedId.hashCode()

  override def toString: String = wrappedId.toString

}