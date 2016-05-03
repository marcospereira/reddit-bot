package controllers

import javax.inject.{ Inject, Singleton }

import models._
import models.Messages._
import play.api.Configuration
import play.api.libs.json._
import play.api.mvc.{ Action, Controller }
import services.{ MessengerService, RedditService }

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class MessengerController @Inject() (
    messengerService: MessengerService,
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
    val futures = request.body.as[ReceivedMessage].entry
      .flatMap(_.messaging)
      .filter(_.message.isDefined)
      .map(messaging => messaging.sender -> messaging.message.get)
      .map { tuple =>
        val sender = tuple._1
        val message = tuple._2
        message.text match {
          case Messages.commandPattern(subreddit, order) => getRedditPosts(subreddit, order, sender)
          case _ => Future(Json.toJson(Messages.help(sender)))
        }
      }
      .map(messengerService.reply)
    Future.sequence(futures).map(responses => Ok("Finished"))
  }

  private def getRedditPosts(subreddit: String, order: String, sender: User): Future[JsValue] = {
    redditService.getSubreddit(subreddit, order, Some(10)).map {
      posts =>
        Json.toJson(
          StructuredMessage(
            recipient = sender,
            message = Map("attachment" -> Attachment.from(posts))
          )
        )
    }
  }
}
