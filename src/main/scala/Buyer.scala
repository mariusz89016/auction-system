import Buyer._
import akka.actor.{Props, Actor, ActorLogging, ActorRef}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Random


object Buyer {
  case class Bid(cash: Int)
  case class ItemBought(description: String)
  case object SendBid

  def props(auctions: Seq[ActorRef]): Props = Props(classOf[Buyer], auctions)
}

class Buyer(private val auctions: Seq[ActorRef]) extends Actor with ActorLogging {
  context.system.scheduler.schedule(1.seconds, 1.seconds, self, SendBid)

  def receive: Receive = {
    case SendBid =>
      val bid = Random.nextInt(300)
      val choice = Random.nextInt(auctions.size)
      auctions(choice) ! Bid(bid)
    case ItemBought(description) =>
      log.debug(s"[${self.path.name}] I bought: $description")
      context.stop(self)
  }
}

