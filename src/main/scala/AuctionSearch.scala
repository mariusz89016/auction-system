import AuctionSearch.{Register, SearchAuction}
import Buyer.Auctions
import akka.actor.{Actor, ActorLogging, ActorRef}

import scala.collection.mutable

object AuctionSearch {
  case class Register(auction: ActorRef, title: String)
  case class SearchAuction(word: String)
}
class AuctionSearch extends Actor with ActorLogging {
  private val registeredAuctions: mutable.Map[ActorRef, String] = mutable.Map()

  def receive: Receive = {
    case Register(auction, title) =>
      registeredAuctions.put(auction, title)
    case SearchAuction(word) =>
      sender() ! Auctions(registeredAuctions.filter(x => x._2.contains(word)))
    case a =>
      log.debug("DOSTALEM " + a)
  }
}
