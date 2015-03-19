import AuctionSearch.SearchAuction
import Buyer.Auctions
import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.WordSpecLike

import scala.concurrent.duration._

class AuctionSystemTest extends TestKit(ActorSystem("auctionSystemTest")) with WordSpecLike with ImplicitSender {
  val auctionNames = Seq("audi", "bmw")

  "Auction system" should {
    "buyer should terminate after win auction" in {
      system.actorOf(Props[AuctionSearch], "auctionSearch")
      system.actorOf(Seller.props(auctionNames), "seller")
      Thread.sleep(800)
      system.actorOf(Buyer.props("audi", 100), "buyer_100")
      val buyer2 = system.actorOf(Buyer.props("audi", 300), "buyer_300")
      val testProbe = TestProbe()
      testProbe.watch(buyer2)

      testProbe.expectTerminated(buyer2, 10 seconds)
    }

    "seller after initializing should register auctions" in {
      val auctionNames = Seq("audi a3", "audi a4", "bmw")
      val auctionSearch = system.actorOf(Props[AuctionSearch], "auctionSearch")
      Thread.sleep(800)
      system.actorOf(Seller.props(auctionNames), "seller")
      Thread.sleep(800)

      auctionSearch ! SearchAuction("audi")
      expectMsgPF(4 seconds) {
        case Auctions(auctions) =>
          auctions.values === Iterable("audi a3", "audi a4")
      }
    }
  }
}
