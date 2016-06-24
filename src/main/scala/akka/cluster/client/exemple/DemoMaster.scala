package akka.cluster.client.exemple

import akka.actor._
import akka.cluster.Cluster
import akka.cluster.client._
import com.typesafe.config._

object DemoMaster {

  def main(args: Array[String]): Unit = {
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
           port = 2551
         }
       }

       cluster {
         seed-nodes = [
           "akka.tcp://ClusterSystem@127.0.0.1:2551"
           ]

         roles = [master]

         auto-down = on
       }
       extensions = ["akka.cluster.client.ClusterClientReceptionist"]
     }""")

    val system = ActorSystem("ClusterSystem", ConfigFactory.load(config))
    Cluster(system)
    system.log.info(s"$config")
    val master = system.actorOf(Props[ClusterMaster], "master")
    ClusterClientReceptionist(system).registerService(master)
  }

  class ClusterMaster extends Actor with ActorLogging {
    context.system.log.info(s"path: ${self.path.toString}")
    def receive = {
      case e =>
        log.info(s"from master : $e : $sender")
        sender() ! "master : how are you?"
    }
  }
}