import akka.actor.{ActorLogging, Actor, Props}


class AuctionManager extends Actor with ActorLogging {
  val NUM_OF_BUYERS = 2
  val NUM_OF_AUCTIONS = 1

  override def receive: Receive = {
    case Start =>
      val auctionList = (0 until NUM_OF_AUCTIONS)
        .map(num => context.actorOf(Props(new Auction(randomSample.randomString(10))), "auction" + num))
        .toList
      val buyerList = (0 until NUM_OF_BUYERS)
        .map(num => context.actorOf(Props(new Buyer(auctionList)), "buyer" + num))
        .toList
      log.debug("auction & buyers started")
    case Stop =>
      log.debug("CLOSING whole ActorSystem...")
      context.system.shutdown()
  }
}

//https://gist.github.com/mahata/4145905
object randomSample {
  def randomString(length: Int) = Stream.continually(util.Random.nextPrintableChar()) take length mkString
}
