import AuctionManager._
import akka.actor.{Actor, ActorLogging}

object AuctionManager {
  sealed trait AuctionManagerMessage
  case object Start extends AuctionManagerMessage
  case object Stop extends AuctionManagerMessage
}

class AuctionManager extends Actor with ActorLogging {
  val NumberOfBuyers = 2
  val NumberOfAuctions = 1

  override def receive: Receive = {
    case Start =>
      val auctionList = (0 until NumberOfAuctions)
        .map(num => context.actorOf(Auction.props(RandomSample.randomString(10)) , "auction" + num))
      (0 until NumberOfBuyers)
        .map(num => context.actorOf(Buyer.props(auctionList), "buyer" + num))
      log.debug("auction & buyers started")
    case Stop =>
      log.debug("CLOSING whole ActorSystem...")
      context.system.shutdown()
  }
}

//https://gist.github.com/mahata/4145905
object RandomSample {
  def randomString(length: Int) = Stream.continually(util.Random.nextPrintableChar()) take length mkString
}
