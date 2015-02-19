package controllers

import actors.UserSocketActor
import com.typesafe.plugin
import models.User
import play.api.mvc.WebSocket.FrameFormatter
import play.api.mvc._
import play.api.Play.current
import com.typesafe.plugin.RedisPlugin
import play.api.libs.json._
import scala.concurrent.Future
import scala.util.control.NonFatal

object Application extends Controller {

	/**
	 * Welcome page.
	 * @return
	 */
  def index = Action {
    Ok(views.html.index())
  }

	/**
	 * Launch the client.
	 * @return
	 */
	def client = Action{ implicit request =>
		Ok(views.html.client())
	}

	// In order to handle input in case it not in json format.
	implicit val myJsonFrame: FrameFormatter[JsValue] = implicitly[FrameFormatter[String]].transform(Json.stringify, { text =>
		try {
			Json.parse(text)
		} catch {
			case NonFatal(e) => Json.obj("error" -> e.getMessage)
		}
	})

	/**
	 * Handle socket connection.
	 * @return
	 */
	def socket = WebSocket.tryAcceptWithActor[JsValue, JsValue] { request =>
		Future.successful(
			param("username", request) match {
				case None => Left(Forbidden)
				case Some(username) => Right(out => UserSocketActor.props(out, User(username)))
			}
		)
	}

	/**
	 * Reset the redis.
	 * @return Action
	 */
	def reset = Action {
		val pool = plugin.use[RedisPlugin].sedisPool
		pool.withClient { client =>
			client.flushAll
		}

		Ok("Reset complete!")
	}

	/**
	 * Get query parameter from a request.
	 * @param field field
	 * @param request request
	 * @return Option[String]
	 */
	def param(field: String, request: RequestHeader): Option[String] =
		request.queryString.get(field).flatMap(_.headOption)


	/**
	 * Test redis.
	 * @return
	 */
	def redis = Action { implicit request =>
		val pool = plugin.use[RedisPlugin].sedisPool

		var test:String = "none"
		pool.withClient { client =>

			client.set("single","2")
			client.del("single")
			val single:Option[String] = client.get("single")
			test = single.getOrElse("not found")
		}

		Ok("redis test: "+test)
	}
}