import Auction._
import AuctionManager.Stop
import Buyer.{Bid, ItemBought}
import akka.actor._

import scala.concurrent.duration._

object Auction {
  case object BidTimerExpired
  case object DeleteTimerExpired
  case object Relist

  sealed trait State
  case object Created extends State
  case object Ignored extends State
  case object Activated extends State
  case object Sold extends State

  sealed trait Data
  case object Uninitialized extends Data
  case class Offer(ref: ActorRef, bid: Int) extends Data
}

class Auction(private val description: String) extends Actor with FSM[State, Data] with ActorLogging {
  private val bidTimeout: FiniteDuration = 3.seconds
  private val deleteTimeout: FiniteDuration = 3.seconds

  startWith(Created, Uninitialized)

  when(Created, bidTimeout) {
    case Event(StateTimeout, _) =>
      log.debug(s"[${self.path.name}] bid time exceeded!")
      goto(Ignored)
    case Event(Bid(x: Int), _) =>
      printActualOffer(self, sender(), 0, x)
      goto(Activated) using Offer(sender(), x)
  }

  when(Ignored, deleteTimeout) {
    case Event(StateTimeout, _) =>
      log.debug(s"[${self.path.name}] killing...")
      context.stop(self)
      stay()
    case Event(Relist, _) =>
      log.debug(s"[${self.path.name}] relisted")
      goto(Created)
  }

  when(Activated) {
    case Event(StateTimeout, Offer(ref, _)) =>
      ref ! ItemBought(description)
      goto(Sold)
    case Event(Bid(newBid: Int), Offer(ref, oldBid)) =>
      printActualOffer(self, sender(), oldBid, newBid)
      if(newBid>oldBid) {
        setTimer("bidTimeout", StateTimeout, bidTimeout, repeat = false)
        goto(Activated) using Offer(sender(), newBid)
      }
      else
        stay()
  }

  when(Sold) {
    case Event(StateTimeout, _) =>
      log.debug(s"[${self.path.name}] killing...")
      context.parent ! Stop
      context.stop(self)
      stay()
    case Event(Bid(_),_) =>
      log.debug("SOLD - offer didn't accept")
      stay()
  }

  onTransition {
    case _ -> Sold =>
      setTimer("deleteTimeout", StateTimeout, deleteTimeout, repeat = false)

  }

  private def printActualOffer(me: ActorRef, sender: ActorRef, oldBid: Int, newBid: Int): Unit = {
    if(oldBid>newBid) {
//      log.debug("-----------------\n[${me.path.name}] too LOW - oldBid: $oldBid newBid: $newBid\n from $sender")
    }
    else {
      log.debug(s"-----------------\n[${me.path.name}] ACCEPT NEW BID - oldBid: $oldBid newBid: $newBid\n from $sender")
    }
  }
}