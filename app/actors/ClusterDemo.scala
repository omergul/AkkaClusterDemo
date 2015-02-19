package actors

import akka.actor._
import akka.cluster._
import akka.cluster.ClusterEvent._
import com.typesafe.config.{Config, ConfigFactory}
import play.api.Logger
import play.libs.Akka

/**
 * Created by Ömer Faruk Gül on 28/01/15.
 */
object ClusterDemo {

	var cfg = ConfigFactory.load()
	val seed = cfg.getBoolean("seed")

	/*
	val seed =
	if(seed) {
		cfg = remoteConfig("127.0.0.1", 2551, cfg)
	}
	else {
		cfg = remoteConfig("127.0.0.1", 2552, cfg)
	}
	val actorSystem = ActorSystem("test", cfg)
	cluster.joinSeedNodes(List(AddressFromURIString("akka.tcp://demo@127.0.0.1:2551")))
	*/

	val actorSystem = Akka.system()
	val cluster = Cluster(actorSystem)

	// if we are the seed, join ourselves
	if(seed) {
		cluster.joinSeedNodes(List(cluster.selfAddress))
	}

	def start() = {
		val listener = actorSystem.actorOf(Props[ClusterListener], "cluster-listener")
	}

	def stop() = {
		actorSystem.shutdown()
	}
}

/**
 * An actor that listens the cluster events.
 */
class ClusterListener extends Actor {

	val cluster = Cluster(context.system)

	override def preStart(): Unit = {
		Logger.debug("Listener actor PATH: "+ self.path.toString)
		Logger.debug("Listener actor CLUSTER: "+ cluster.selfAddress.toString)

		cluster.subscribe(self, classOf[ClusterDomainEvent])
	}

	def receive = {
		case state: CurrentClusterState =>
		case MemberUp(member) =>
			Logger.info(s"Member is Up: ${member}")
		case UnreachableMember(member) =>
			Logger.warn(s"Member is unreachable: ${member}")
		case MemberRemoved(member, previousStatus) =>
			Logger.warn(s"Member is Removed: ${member.address} after ${previousStatus}")
		case _: ClusterDomainEvent => // ignore
	}
}
