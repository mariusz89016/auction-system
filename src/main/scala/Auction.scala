import Auction._
import AuctionManager.AuctionEnd
import Buyer.{Outbidden, Bid, ItemBought}
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

  def props(description: String): Props = Props(classOf[Auction], description)
}

class Auction(description: String) extends Actor with FSM[State, Data] with ActorLogging {
  private val bidTimeout: FiniteDuration = 3.seconds
  private val deleteTimeout: FiniteDuration = 3.seconds

  startWith(Created, Uninitialized)

  when(Created, bidTimeout) {
    case Event(StateTimeout, _) =>
      log.debug(s"[${self.path.name}] bid time exceeded!")
      goto(Ignored)
    case Event(Bid(x: Int), _) =>
      printAcceptedBid(self, sender(), 0, x)
      setTimer("bidTimeout", StateTimeout, bidTimeout, repeat = false)
      goto(Activated) using Offer(sender(), x)
  }

  when(Ignored, deleteTimeout) {
    case Event(StateTimeout, _) =>
      log.debug(s"[${self.path.name}] killing...")
      stop()
    case Event(Relist, _) =>
      log.debug(s"[${self.path.name}] relisted")
      goto(Created)
  }

  when(Activated) {
    case Event(StateTimeout, Offer(ref, _)) =>
      ref ! ItemBought(description)
      goto(Sold)
    case Event(Bid(newBid: Int), Offer(ref, oldBid)) =>
      if(newBid>oldBid) {
        printAcceptedBid(self, sender(), oldBid, newBid)
        if(ref != sender()) {
          ref ! Outbidden(self, newBid)
        }
        setTimer("bidTimeout", StateTimeout, bidTimeout)
        goto(Activated) using Offer(sender(), newBid)
      }
      else
        stay()
  }

  when(Sold) {
    case Event(StateTimeout, _) =>
      log.debug(s"[${self.path.name}] killing...")
      context.parent ! AuctionEnd
      stop()
    case Event(Bid(_),_) =>
      val message = s"[${self.path.name}] SOLD - offer didn't accept"
      sender() ! message
      log.debug(message)
      stay()
  }

  onTransition {
    case _ -> Sold =>
      if (!isTimerActive("deleteTimeout")) {
        setTimer("deleteTimeout", StateTimeout, deleteTimeout)
      }

  }

  private def printAcceptedBid(me: ActorRef, sender: ActorRef, oldBid: Int, newBid: Int): Unit = {
    if(oldBid<newBid) {
      log.debug(s"""
      ┌----------------
      |[${me.path.name}] ACCEPTED NEW BID - oldBid: $oldBid newBid: $newBid")
      |from $sender
      └----------------""")
    }
  }
  initialize()
}