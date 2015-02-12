import akka.actor.{ActorLogging, Cancellable, Actor, ActorRef}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Random
import Buyer._


object Buyer {
  case class Bid(cash: Int)
  case class ItemBought(description: String)
}

class Buyer(private val auctions: List[ActorRef]) extends Actor with ActorLogging {
  private val scheduledTask: Cancellable = context.system.scheduler.schedule(1.second, 1.seconds) {
    val bid = Random.nextInt(300)
    val choice = Random.nextInt(auctions.size)
    auctions(choice) ! Bid(bid)
  }

  override def postStop(): Unit = {
    scheduledTask.cancel()
  }

  def receive: Receive = {
    case ItemBought(description: String) =>
      log.debug(s"[${self.path.name}] I bought: $description")
      context.stop(self)
  }
}

