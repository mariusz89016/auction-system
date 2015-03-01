import AuctionSearch.SearchAuction
import Buyer._
import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Random


object Buyer {
  case class Auctions(auctions: mutable.Map[ActorRef, String])
  case class Bid(cash: Int)
  case class ItemBought(description: String)
  case object SendBid
  case class Outbidden(auction: ActorRef, actualBid: Int)

  def props(word: String, maxCash: Int): Props = Props(classOf[Buyer], word, maxCash)
}

class Buyer(private val word: String, private val maxCash: Int) extends Actor with ActorLogging {
  context.actorSelection("/user/auctionManager/auctionSearch") ! SearchAuction(word)
  private var auctions: mutable.Map[ActorRef, String] = null


  def receive: Receive = {
    case Auctions(retrievedAuctions) =>
      auctions = retrievedAuctions
      context.system.scheduler.schedule(1.seconds, 1.seconds, self, SendBid)
      context become bidding
  }

  def bidding: Receive = {
    case SendBid =>
      val bid = Random.nextInt(maxCash)
      Random.shuffle(auctions.keys.toList).head ! Bid(bid)
    case ItemBought(description) =>
      log.debug(s"[${self.path.name}] I bought: $description")
      context.stop(self)
    case Outbidden(auction, bid) =>
      if (bid<maxCash) {
        auction ! Random.nextInt(maxCash - bid)+bid
      }
  }
}

