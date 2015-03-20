import MasterSearch.{SearchAuction, Register}
import akka.actor._
import akka.routing.{BroadcastRoutingLogic, RoundRobinRoutingLogic, Router, ActorRefRoutee}

class MasterSearch(isReplication: Boolean) extends Actor with ActorLogging{
  val actorRefRoutees = Vector.fill(5) {
    val routee = context.actorOf(Props[AuctionSearch])
    context watch routee
    ActorRefRoutee(routee)
  }
  var registerRouter = {
    if(isReplication)
      Router(BroadcastRoutingLogic(), actorRefRoutees)
    else
      Router(RoundRobinRoutingLogic(), actorRefRoutees)
  }

  var searchRouter = {
    if(isReplication)
      Router(RoundRobinRoutingLogic(), actorRefRoutees)
    else
      Router(BroadcastRoutingLogic(), actorRefRoutees)
  }

  def receive: Receive = {
    case Register(auction, title) =>
      log.info("ZAREJESTROWANO " + title)
      registerRouter.route(AuctionSearch.Register(auction,title), sender())
    case SearchAuction(word) =>
      searchRouter.route(AuctionSearch.SearchAuction(word), sender())
  }
}

object MasterSearch {
  case class Register(auction: ActorRef, title: String)
  case class SearchAuction(word: String)

  def props(isReplication: Boolean): Props = Props(classOf[MasterSearch], isReplication)
}
