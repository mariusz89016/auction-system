import Auction._
import Buyer.Bid
import akka.actor.ActorSystem
import akka.testkit.{TestFSMRef, TestKit}
import org.scalatest.{BeforeAndAfter, WordSpecLike}

import scala.concurrent.duration._

class AuctionTest extends TestKit(ActorSystem("AuctionTest")) with WordSpecLike with BeforeAndAfter {
  var fsm: TestFSMRef[State, Data, Auction] = null

  before {
    fsm = TestFSMRef(new Auction("Audi A6 diesel manual"))
  }

  after {
    fsm.stop()
  }

  "Auction FSM" should {
    "have Created as initial state" in {
      assert(fsm.stateName == Created)
    }
    "have Uninitialized as initial data" in {
      assert(fsm.stateData == Uninitialized)
    }
    "after bidTimeout should go to Ignored state" in {
      awaitCond(fsm.stateName == Ignored, 3100 milliseconds)
    }
    "go to Activated state when receive bid" in {
      fsm ! Bid(10)
      assert(fsm.stateName == Activated)
    }
  }
}
