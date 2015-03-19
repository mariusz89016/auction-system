import AuctionSearch.{Register, SearchAuction}
import Buyer.Auctions
import akka.actor.{Actor, ActorLogging, ActorRef}


object AuctionSearch {
  case class Register(auction: ActorRef, title: String)
  case class SearchAuction(word: String)
}
class AuctionSearch extends Actor with ActorLogging {
  private val registeredAuctions: collection.mutable.Map[ActorRef, String] = collection.mutable.Map()

  def receive: Receive = {
    case Register(auction, title) =>
      registeredAuctions.put(auction, title)
    case SearchAuction(word) =>
      val filteredMap = registeredAuctions.filter(x => x._2.contains(word))
      sender() ! Auctions(filteredMap.toMap)
    case a =>
      log.debug("DOSTALEM " + a)
  }
}
