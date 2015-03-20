import AuctionManager._
import akka.actor.{Actor, ActorLogging, Props}

import scala.util.Random

object AuctionManager {
  sealed trait AuctionManagerMessage
  case object Start extends AuctionManagerMessage
  case object AuctionEnd extends AuctionManagerMessage

  def props(amountOfSellers: Int,
            amountOfBuyers: Int,
            auctionNames: Seq[String],
            keywords: Seq[String],
            maxCash: Int): Props =
    Props(classOf[AuctionManager],
      amountOfSellers,
      amountOfBuyers,
      auctionNames,
      keywords,
      maxCash)
}

class AuctionManager(amountOfSellers: Int,
                     amountOfBuyers: Int,
                     auctionNames: Seq[String],
                     keywords: Seq[String],
                     maxCash: Int) extends Actor with ActorLogging {
  override def receive: Receive = {
    case Start =>
      context.actorOf(MasterSearch.props(isReplication = true), "masterSearch")
      Thread.sleep(800)
      createSellers()
      Thread.sleep(800)
      createBuyers()
  }

  def createSellers() = {
    val numberOfAuctionsPerSeller = math.ceil(auctionNames.length/amountOfSellers.toFloat).toInt
    val sellersAuctions = auctionNames.grouped(numberOfAuctionsPerSeller).toList

    sellersAuctions.zipWithIndex.foreach { case (seqForSeller, i) =>
      context.actorOf(Seller.props(seqForSeller), s"seller$i")
    }
  }

  def createBuyers() = {
    for(i <- 0 until amountOfBuyers) {
      val keyword = Random.shuffle(keywords).head
      context.actorOf(Buyer.props(keyword, Random.nextInt(maxCash)+1), s"buyer${i}_$keyword")
    }
  }
}

//https://gist.github.com/mahata/4145905
object RandomSample {
  def randomString(length: Int) = Stream.continually(util.Random.nextPrintableChar()) take length mkString
}
