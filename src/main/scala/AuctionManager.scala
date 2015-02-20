import AuctionManager._
import akka.actor.{Props, Actor, ActorLogging}

object AuctionManager {
  sealed trait AuctionManagerMessage
  case object Start extends AuctionManagerMessage
  case object AuctionEnd extends AuctionManagerMessage
}

class AuctionManager extends Actor with ActorLogging {
  val sellerAuctions1 = Seq("Audi A6 diesel manual", "BMW benzyna automat", "Lotus gaz manual")
  val sellerAuctions2 = Seq("Audi A1 benzyna automat", "BMW gaz manual", "Lotus diesiel automat")

  override def receive: Receive = {
    case Start =>
      val auctionSearchRef = context.actorOf(Props[AuctionSearch], "auctionSearch")

      context.actorOf(Seller.props(sellerAuctions1), "seller1")
      context.actorOf(Seller.props(sellerAuctions2), "seller2")

      context.actorOf(Buyer.props("diesel"), "buyer_diesel")
      context.actorOf(Buyer.props("manual"), "buyer_manual")
      context.actorOf(Buyer.props("Audi"), "buyer_audi")
      context.actorOf(Buyer.props("BMW"), "buyer_bmw")

      log.debug("auctionSearch, sellers & buyers started")
  }
}

//https://gist.github.com/mahata/4145905
object RandomSample {
  def randomString(length: Int) = Stream.continually(util.Random.nextPrintableChar()) take length mkString
}
