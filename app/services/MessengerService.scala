package services

import javax.inject.{ Inject, Singleton }

import models.Messages._
import models.{ Messages, User }
import play.api.Configuration
import play.api.http.ContentTypes
import play.api.libs.json.{ JsValue, Json }
import play.api.libs.ws.{ WSClient, WSResponse }
import play.api.http.HeaderNames._

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class MessengerService @Inject() (config: Configuration, ws: WSClient)(implicit executionContext: ExecutionContext) {

  def reply(f: Future[JsValue]): Future[WSResponse] = f.flatMap { json =>
    post(json).flatMap { response =>
      if (success(response)) Future(response)
      else replyWithError(json, response.json)
    }
  }

  private def replyWithError(body: JsValue, error: JsValue): Future[WSResponse] = {
    val sender = (body \ "recipient").as[User]
    val err = (error \ "error").as[models.Error]
    val errorMessage = Messages.oops(sender, err.message)
    post(Json.toJson(errorMessage))
  }

  private def post(body: JsValue): Future[WSResponse] = ws.url(config.getString("facebook.messages.url")
    .getOrElse("https://graph.facebook.com/v2.6/me/messages"))
    .withQueryString("access_token" -> config.getString("facebook.messages.token").getOrElse(""))
    .withHeaders(CONTENT_TYPE -> ContentTypes.JSON)
    .withRequestFilter(play.api.libs.ws.ahc.AhcCurlRequestLogger())
    .withRequestFilter(AhcResponseLogger(body = true))
    .post(body)

  private def success(response: WSResponse): Boolean = response.status >= 200 && response.status < 300
}
