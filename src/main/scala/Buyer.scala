import Buyer._
import MasterSearch.SearchAuction
import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Random


object Buyer {
  case class Auctions(auctions: Map[ActorRef, String])
  case class Bid(cash: Int)
  case class ItemBought(description: String)
  case object SendBid
  case class Outbidden(auction: ActorRef, actualBid: Int)

  def props(word: String, maxCash: Int): Props = Props(classOf[Buyer], word, maxCash)
}

class Buyer(word: String, maxCash: Int) extends Actor with ActorLogging {
  context.actorSelection("../masterSearch") ! SearchAuction(word)
  private val auctions: collection.mutable.Map[ActorRef, String] = collection.mutable.Map()

  context.system.scheduler.schedule(1.seconds, 1.seconds, self, SendBid)

  def receive: Receive = {
    case Auctions(retrievedAuctions) =>
      auctions ++= retrievedAuctions
      println(word + "\n"+retrievedAuctions)
    case SendBid =>
      val bid = Random.nextInt(maxCash)+1
      Random.shuffle(auctions.keys.toList).head ! Bid(bid)
    case ItemBought(description) =>
      log.debug(s"[${self.path.name}] I bought: $description")
      context.stop(self)
    case Outbidden(auction, bid) =>
      if (bid<maxCash) {
        auction ! Bid(Random.nextInt(maxCash - bid)+bid)
      }
  }
}

