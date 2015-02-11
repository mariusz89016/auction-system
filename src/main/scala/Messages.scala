import akka.actor.ActorRef

// AuctionManager ---------
sealed trait AuctionManagerMessage
case object Start extends AuctionManagerMessage
case object Stop extends AuctionManagerMessage


// FSM --------------------
sealed trait State
case object Created extends State
case object Ignored extends State
case object Activated extends State
case object Sold extends State

sealed trait Data
case object Uninitialized extends Data
case class Offer(ref: ActorRef, bid: Int) extends Data


// Auction ----------------
sealed trait AuctionMessage
case object BidTimerExpired extends AuctionMessage
case object DeleteTimerExpired extends AuctionMessage
case object Relist extends AuctionMessage


// Buyer ------------------
sealed trait BuyerMessage
case class Bid(cash: Int) extends BuyerMessage
case class ItemBought(description: String) extends BuyerMessage