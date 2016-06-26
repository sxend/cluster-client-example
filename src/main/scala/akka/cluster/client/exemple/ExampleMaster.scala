package akka.cluster.client.exemple

import akka.actor._
import akka.cluster.client._
import com.typesafe.config._

object ExampleMaster {

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load("master.conf")
    val system = ActorSystem("ClusterSystem", config)
    val master = system.actorOf(Props[ClusterMaster], "master")
    ClusterClientReceptionist(system).registerService(master)
  }

  class ClusterMaster extends Actor with ActorLogging {
    def receive = {
      case e =>
        log.info(s"receive message: [$e] from $sender")
        sender() ! s"master's response of [$e]"
    }
  }
}