import akka.actor.{Actor, ActorLogging, Props}

object Seller {
  def props(auctions: Seq[String]) = Props(classOf[Seller], auctions)
}
class Seller(private val auctions: Seq[String]) extends Actor with ActorLogging {
  require(auctions.size > 0)

  auctions.foreach(auctionName => context.actorOf(Auction.props(auctionName)))

  def receive: Receive = {
    case _ => s"I'm seller[$self}]!"
  }
}
