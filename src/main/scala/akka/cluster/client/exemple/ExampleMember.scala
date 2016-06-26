package akka.cluster.client.exemple

import akka.actor._
import akka.cluster.client._
import com.typesafe.config._

object ExampleMember {

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load("member.conf")
    val system = ActorSystem("ClusterSystem", config)
    val member = system.actorOf(Props[ClusterMember], "member")
    ClusterClientReceptionist(system).registerService(member)
  }

  class ClusterMember extends Actor with ActorLogging {
    def receive = {
      case e =>
        log.info(s"receive message: [$e] from $sender")
        sender() ! s"member's response of [$e]"
    }
  }
}