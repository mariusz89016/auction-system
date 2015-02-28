import Auction._
import Buyer.{ItemBought, Bid}
import akka.actor.{Terminated, ActorSystem}
import akka.testkit.{TestProbe, ImplicitSender, TestFSMRef, TestKit}
import org.scalatest.{BeforeAndAfter, Tag, WordSpecLike}

import scala.concurrent.duration._

object AwaitCondTest extends Tag("AwaitCondTest")

class AuctionTest extends TestKit(ActorSystem("AuctionTest")) with WordSpecLike with BeforeAndAfter with ImplicitSender {
  var fsm: TestFSMRef[State, Data, Auction] = null
  val description = "Audi A6 diesel manual"

  before {
    fsm = TestFSMRef(new Auction(description))
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
        awaitCond(fsm.stateName == Ignored, 4 seconds)
      }
      "go to Activated state when receive bid" in {
        fsm ! Bid(10)
        assert(fsm.stateName == Activated)
      }
    }
    "in Ignored state" should {
      "stop after bidTimeout" taggedAs AwaitCondTest in {
        val probe = new TestProbe(system)
        probe.watch(fsm)
        probe.expectMsgPF(8 seconds) { case Terminated(fsm) => true}

      }
    }
    "in Activated state" should {
      "go to Sold state after bidTimeout" taggedAs AwaitCondTest in {
        fsm ! Bid(10)
        awaitCond(fsm.stateName == Sold, 4 seconds)
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
      "stop after deleteTimeout" in {
        val probe = TestProbe()
        probe.watch(fsm)
        probe.expectMsgPF(8 seconds) { case Terminated(fsm) => true}
      }
      "stay in Sold" in {
        fsm ! Bid(10)
        expectMsg(4 seconds, ItemBought(description))
      }
    }

  }
}
