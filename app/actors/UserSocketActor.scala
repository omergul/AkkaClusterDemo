package actors
import akka.actor._
import akka.cluster.Cluster
import com.typesafe.plugin
import play.api.Logger
import models.User
import play.api.Play.current
import com.typesafe.plugin.RedisPlugin
import play.api.libs.json._
import akka.contrib.pattern.DistributedPubSubExtension
import akka.contrib.pattern.DistributedPubSubMediator.{Publish, Subscribe, SubscribeAck}
import scala.concurrent.duration._

/**
 * Created by Ömer Faruk Gül on 25/01/15.
 */

object UserSocketActor {
	def props(out: ActorRef, user: User) = Props(new UserSocketActor(out, user))
}

class UserSocketActor(out: ActorRef, user: User) extends Actor {

	val code:String = user.generateCode
	val cluster = Cluster(context.system)

	val mediator = DistributedPubSubExtension(context.system).mediator
	// subscribe to our username
	mediator ! Subscribe(user.username, self)
	// subscribe to general events
	mediator ! Subscribe("general", self)

	override def preStart(): Unit = {
		Logger.debug("User actor started with code: "+ code)
		Logger.debug("User actor PATH: "+ self.path.toString)
		Logger.debug("User actor CLUSTER: "+ cluster.selfAddress.toString)

		val pool = plugin.use[RedisPlugin].sedisPool

		pool.withClient { client =>

			client.sadd("user:"+user.username, code)
			client.hset("users", user.username, "1")

			//val usersMap: Set[String] = client.hkeys("users")
			//Logger.debug("Users map: "+usersMap.mkString(","));
		}

		import context.dispatcher
		cluster.system.scheduler.scheduleOnce(1.seconds) {
			// publish so that other users update their user list.
			mediator ! Publish("general", Json.toJson(Map("event" -> "users")))
		}
	}

	def receive = {

		case SubscribeAck(Subscribe(user.username, None, self)) =>
			Logger.debug("Subscribe is successful!")

		case msg: JsValue =>
			Logger.debug("js value: "+ msg)

			val error = (msg \ "error").asOpt[String]

			if(error.isDefined) {
				out ! Json.obj("error" -> "Input json format error.")
			}
			else {
				(msg \ "event").asOpt[String] match {
					case Some("hi") =>
						out ! Json.obj("event" -> "hi")
					case Some("users") =>
						val pool = plugin.use[RedisPlugin].sedisPool
						pool.withClient { client =>
							val usersSet: Set[String] = client.hkeys("users").filter(username => username != user.username)
							val usersList: Set[Map[String, String]] = usersSet.map(key => Map("username" -> key, "isOnline" -> client.hget("users", key)))
							//Logger.debug("Users map count : "+usersMap.size)
							out ! Json.obj("event" -> "users", "users" -> Json.toJson(usersList))
						}
					case Some("talk") =>
						(msg \ "to").asOpt[String] match {
							case (Some(to)) =>
								// received a message
								if(to == user.username) {
									out ! msg
								}
								// sending a message
								else {
									Logger.debug("Publishing to : "+to)
									mediator ! Publish(to, msg)
									out ! Json.obj("event" -> "published")
								}
							case _ =>
								out ! Json.obj("error" -> "Missing parameters.")
						}

					case Some(event) =>
						out ! Json.obj("error" -> Json.toJson("Unknown event: %s" format event))
					case None =>
						out ! Json.obj("error" -> "Undefined event.")
				}
			}
		case msg =>
			Logger.debug("Unknown message: "+msg)
	}

	override def postStop(): Unit = {
		Logger.debug("User actor stopped: "+ code)

		val pool = plugin.use[RedisPlugin].sedisPool
		pool.withClient { client =>
			// remove the user
			client.srem("user:"+user.username, code)
			val userMembers = client.smembers("user:"+user.username)

			// If there are no more members, it means this user has no other instance (multiple client)
			// Simply tag him offline.
			if(userMembers.size == 0) {
				client.hset("users", user.username, "0")
				mediator ! Publish("general", Json.toJson(Map("event" -> "users")))
			}
		}
	}

}
