import AuctionManager.Start
import akka.actor.ActorSystem

object Runner {
  val system = ActorSystem()
  def main (args: Array[String]) {
    val AmountOfSellers = 2
    val AmountOfBuyers = 4
    val AuctionsName = Seq(
      "Audi A6 diesel manual",
      "BMW benzyna automat",
      "Lotus gaz manual",
      "Audi A1 benzyna automat",
      "BMW gaz manual",
      "Lotus diesel automat"
    )
    val Keywords = Seq(
      "diesel",
      "manual",
      "Audi",
      "BMW"
    )
    val MaxCash = 300

    val auctionManager = system.actorOf(
      AuctionManager.props(
        AmountOfSellers,
        AmountOfBuyers,
        AuctionsName,
        Keywords,
        MaxCash),
      "auctionManager")

    auctionManager ! Start
  }
}
