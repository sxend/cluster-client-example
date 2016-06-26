package akka.cluster.client.exemple

import akka.actor._
import akka.cluster.client._
import akka.pattern._
import akka.util.Timeout
import com.typesafe.config._

import scala.concurrent.duration._
import scala.util.{ Failure, Success, Try }

object ExampleClient {

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load("client.conf")
    implicit val system = ActorSystem("AnotherSystem", config)
    import system.dispatcher
    implicit val timeout = Timeout(10.seconds)

    val initialContacts = Set(
      ActorPath.fromString("akka.tcp://ClusterSystem@127.0.0.1:2551/system/receptionist"),
      ActorPath.fromString("akka.tcp://ClusterSystem@127.0.0.1:3000/system/receptionist")
    )
    val settings =
      ClusterClientSettings(system).withInitialContacts(initialContacts)

    val client = system.actorOf(ClusterClient.props(settings), "client")

    system.scheduler.schedule(1.seconds, 1.seconds) {
      client.ask(ClusterClient.Send("/user/master", s"ask to master($timestamp)", localAffinity = true))
        .mapTo[String].onComplete(complete)
      client.ask(ClusterClient.Send("/user/member", s"ask to member($timestamp)", localAffinity = false))
        .mapTo[String].onComplete(complete)
    }
  }
  private def complete(implicit system: ActorSystem): PartialFunction[Try[String], Unit] = {
    case Success(message) => system.log.info(s"ask complete: [$message]")
    case Failure(t)       => system.log.error(t, t.getMessage)
  }
  private def timestamp = new java.util.Date().toString
}