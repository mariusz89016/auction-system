import MasterSearch.Register
import akka.actor.{Actor, ActorLogging, Props}

object Seller {
  def props(auctions: Seq[String]) = Props(classOf[Seller], auctions)
}
class Seller(auctions: Seq[String]) extends Actor with ActorLogging {
  require(auctions.size > 0)

  val masterSearch = context.actorSelection("../masterSearch")
  auctions.foreach(auctionName => {
    val auctionRef = context.actorOf(Auction.props(auctionName), auctionName.replaceAll(" ", "_"))
    masterSearch ! Register(auctionRef, auctionName)
  })

  def receive: Receive = {
    case _ => s"I'm seller[$self}]!"
  }
}
