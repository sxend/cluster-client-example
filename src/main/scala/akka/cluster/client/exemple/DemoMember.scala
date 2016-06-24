package akka.cluster.client.exemple

import akka.actor._
import akka.cluster.Cluster
import akka.cluster.client._
import com.typesafe.config._

object DemoMember {

  def main(args: Array[String]) {
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
          port = 3000
         }
       }

       cluster {
         seed-nodes = [
           "akka.tcp://ClusterSystem@127.0.0.1:2551"
           ]

         auto-down = on
       }
       extensions = ["akka.cluster.client.ClusterClientReceptionist"]
     }""")

    val system = ActorSystem("ClusterSystem", ConfigFactory.load(config))
    Cluster(system)
    system.log.info(s"$config")
    val clusterMember = system.actorOf(Props[ClusterMember], "member")
    ClusterClientReceptionist(system).registerService(clusterMember)
  }

  class ClusterMember extends Actor with ActorLogging {
    context.system.log.info(s"path: ${self.path.toString}")
    def receive = {
      case e =>
        log.info(s"from member : $e : $sender")
        sender() ! "member : how are you?"
    }
  }
}