package akka.cluster.client.exemple

import akka.actor._
import akka.cluster.client._
import akka.pattern._
import akka.util.Timeout
import com.typesafe.config._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

object DemoClient {

  def main(args : Array[String]) {
    val config = ConfigFactory.parseString("""
     akka {
       actor {
         provider = "akka.cluster.ClusterActorRefProvider"
       }

       remote {
         transport = "akka.remote.netty.NettyRemoteTransport"
         log-remote-lifecycle-events = off
         netty.tcp {
          hostname = "127.0.0.1"
          port = 5000
         }
       }
       extensions = ["akka.cluster.client.ClusterClientReceptionist"]
     }""")

    implicit val system = ActorSystem("OTHERSYSTEM", ConfigFactory.load(config))
    system.log.info(s"$config")
    import system.dispatcher
    implicit val timeout = Timeout(10.seconds)
    val initialContacts = Set(
      ActorPath.fromString("akka.tcp://ClusterSystem@127.0.0.1:2551/system/receptionist"),
      ActorPath.fromString("akka.tcp://ClusterSystem@127.0.0.1:3000/system/receptionist"))
    val settings = ClusterClientSettings(system)
      .withInitialContacts(initialContacts)

    val c = system.actorOf(ClusterClient.props(settings), "client")

    (1 to 1000).map { i =>
      c.ask(ClusterClient.Send("/user/master", s"hello - $i", localAffinity = true))
        .mapTo[String].onComplete(complete)
      c.ask(ClusterClient.Send("/user/member", s"hello - $i", localAffinity = false))
        .mapTo[String].onComplete(complete)
      Thread.sleep(1000)
    }
  }
  private def complete(result: Try[String])(implicit system: ActorSystem) = result match {
    case Success(message) => system.log.info(message)
    case Failure(t) => system.log.error(t, t.getMessage)
  }
}