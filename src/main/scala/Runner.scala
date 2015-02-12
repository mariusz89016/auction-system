import AuctionManager.Start
import akka.actor.{ActorSystem, Props}

object Runner {
  val system = ActorSystem()
  def main (args: Array[String]) {
    val auctionManager = system.actorOf(Props[AuctionManager], "auctionManager")
    auctionManager ! Start
  }
}
