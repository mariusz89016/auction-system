import Auction._
import Buyer.Bid
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestFSMRef, TestKit}
import org.scalatest.{Tag, BeforeAndAfter, WordSpecLike}

import scala.concurrent.duration._

object AwaitCondTest extends Tag("AwaitCondTest")

class AuctionTest extends TestKit(ActorSystem("AuctionTest")) with WordSpecLike with BeforeAndAfter with ImplicitSender {
  var fsm: TestFSMRef[State, Data, Auction] = null

  before {
    fsm = TestFSMRef(new Auction("Audi A6 diesel manual"))
  }

  after {
    fsm.stop()
  }

  "Auction FSM" when {
    "on start" should {
      "have Created as initial state" in {
        assert(fsm.stateName == Created)
      }
      "have Uninitialized as initial data" in {
        assert(fsm.stateData == Uninitialized)
      }
    }
    "in Created state" should {
      "go to Ignored state after bidTimeout" taggedAs AwaitCondTest in {
        awaitCond(fsm.stateName == Ignored, 3100 milliseconds)
      }
      "go to Activated state when receive bid" in {
        fsm ! Bid(10)
        assert(fsm.stateName == Activated)
      }
    }
    "in Ignored state" should {
      //      "stop after bidTimeout" {
      //        ???
      //      }
    }
    "in Activated state" should {
      "go to Sold state after bidTimeout" taggedAs AwaitCondTest in {
        fsm ! Bid(10)
        awaitCond(fsm.stateName == Sold, 3100 milliseconds)
      }

      "stay in Activated state with new Offer when receive better bid " in {
        val bid = Bid(10)
        val newBid = Bid(20)
        fsm ! bid
        fsm ! newBid
        assert(fsm.stateName == Activated && fsm.stateData == Offer(self, 20))
      }
      "stay in Activated state with old Offer when receive the same or lower bid" in {
        val bid = Bid(20)
        val newBid = Bid(10)
        fsm ! bid
        fsm ! newBid
        assert(fsm.stateName == Activated && fsm.stateData == Offer(self, 20))
      }
    }

    "in Sold state" should {
//      "stop after deleteTimeout" in {
//        ???
//      }
//      "stay in Sold" in {
//        fsm ! Bid(10)
//        //how to receive message?
//      }
    }

  }
}
