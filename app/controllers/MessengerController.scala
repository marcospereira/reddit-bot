package controllers

import javax.inject.{ Inject, Singleton }

import models._
import models.Messages._
import play.api.Configuration
import play.api.http.ContentTypes
import play.api.libs.json._
import play.api.libs.ws.{ WSClient, WSResponse }
import play.api.libs.ws.ahc.AhcCurlRequestLogger
import play.api.mvc.{ Action, Controller }
import services.{ AhcResponseHeadersLogger, RedditService }

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class MessengerController @Inject() (
    ws: WSClient,
    config: Configuration,
    redditService: RedditService
)(implicit executionContext: ExecutionContext) extends Controller {

  def verifyApp = Action { implicit request =>
    val expectedToken = config
      .getString("facebook.app.verifyToken")
      .getOrElse(sys.error("Configuration `facebook.app.verifyToken` was not found."))
    request.getQueryString("hub.verify_token") match {
      case Some(verifyToken) =>
        val challenge = request.getQueryString("hub.challenge").getOrElse("")
        if (verifyToken == expectedToken) Ok(challenge) else Forbidden
      case None => Unauthorized
    }
  }

  def receiveMessage = Action.async(parse.json) { request =>
    val futures = request.body.as[PostedMessage].entry
      .flatMap(_.messaging)
      .filter(_.message.isDefined)
      .map(messaging => messaging.sender -> messaging.message.get)
      .map { tuple =>
        val sender = tuple._1
        val message = tuple._2
        message.text match {
          case Messages.commandFormat(subreddit, order) => redditPosts(subreddit, order, sender)
          case _ => Future(Json.toJson(Messages.help(sender)))
        }
      }
      .map(postBack)
    Future.sequence(futures).map(responses => Ok("Finished"))
  }

  private def redditPosts(subreddit: String, order: String, sender: User): Future[JsValue] = {
    redditService.getSubreddit(subreddit, order, Some(10)).map {
      posts =>
        Json.obj(
          "recipient" -> Json.toJson(sender),
          "message" -> Json.obj(
            "attachment" -> Json.toJson(Attachment.from(posts))
          )
        )
    }
  }

  private def postBack(f: Future[JsValue]): Future[WSResponse] = {
    f.flatMap { response =>
      ws.url(config.getString("facebook.messages.url").getOrElse("https://graph.facebook.com/v2.6/me/messages"))
        .withQueryString("access_token" -> config.getString("facebook.messages.token").getOrElse(""))
        .withHeaders(CONTENT_TYPE -> ContentTypes.JSON)
        .withRequestFilter(AhcCurlRequestLogger())
        .withRequestFilter(AhcResponseHeadersLogger())
        .post(response)
    }
  }
}
