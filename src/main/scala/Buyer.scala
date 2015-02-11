import akka.actor.{Cancellable, Actor, ActorRef}

import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.duration._
import scala.util.Random


class Buyer(private val auctions: List[ActorRef]) extends Actor{
  private var scheduledTask: Cancellable = null

  override def preStart(): Unit = {
    scheduledTask = context.system.scheduler.schedule(1.seconds, 1.seconds) {
      val bid = Random.nextInt(300)
      val choice = Random.nextInt(auctions.size)
//      println("poszedl bid: " + bid + " do: " + auctions(choice))
      auctions(choice) ! Bid(bid)
    }
  }

  override def postStop(): Unit = {
    scheduledTask.cancel()
  }

  def receive: Receive = {
    case ItemBought(description: String) =>
      println("[" + self.path.name + "] I bought \n" + description)
      context.stop(self)
  }
}

