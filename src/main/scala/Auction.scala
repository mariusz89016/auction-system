import akka.actor._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class Auction(private val description: String) extends Actor with FSM[State, Data] with ActorLogging {
  private var bidTimer: Cancellable = null
  private var deleteTimer: Cancellable = null

  override def preStart(): Unit = {
    bidTimer = context.system.scheduler.scheduleOnce(
      3.seconds,
      self,
      BidTimerExpired
    )
  }

  startWith(Created, Uninitialized)

  when(Created) {
    case Event(Bid(x: Int), _) =>
      bidTimer.cancel()
      bidTimer = context.system.scheduler.scheduleOnce(
        3.seconds,
        self,
        BidTimerExpired
      )
      printActualOffer(self, sender(), 0, x)
      goto(Activated) using Offer(sender(), x)
    case Event(BidTimerExpired, _) =>
      log.debug(s"[${self.path.name}] bid time exceeded!")
      deleteTimer = context.system.scheduler.scheduleOnce(
        5.seconds,
        self,
        DeleteTimerExpired
      )
      goto(Ignored)
  }

  when(Ignored) {
    case Event(DeleteTimerExpired, _) =>
      log.debug(s"[${self.path.name}] killing...")
      context.stop(self)
      stay()
    case Event(Relist, _) =>
      log.debug(s"[${self.path.name}] relisted")
      goto(Created)
  }

  when(Activated) {
    case Event(Bid(newBid: Int), Offer(ref, oldBid)) =>
      printActualOffer(self, sender(), oldBid, newBid)
      if(newBid>oldBid) {
        bidTimer.cancel()
        bidTimer = context.system.scheduler.scheduleOnce(
          3.seconds,
          self,
          BidTimerExpired
        )
        goto(Activated) using Offer(sender(), newBid)
      }
      else
        stay() using Offer(ref, oldBid)
    case Event(BidTimerExpired, Offer(ref, _)) =>
      ref ! ItemBought(description)
      deleteTimer = context.system.scheduler.scheduleOnce(
        5.seconds,
        self,
        DeleteTimerExpired
      )
      goto(Sold)
  }

  when(Sold) {
    case Event(DeleteTimerExpired, _) =>
      log.debug(s"[${self.path.name}] killing...")
      context.parent ! Stop
      context.stop(self)
      stay()
    case Event(Bid(_),_) =>
      log.debug("SOLD - offer didn't accept")
      stay()
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