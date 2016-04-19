package controllers

import javax.inject.{ Inject, Singleton }

import play.api.Configuration
import play.api.mvc.Action
import play.api.mvc.Controller

@Singleton
class CallbacksController @Inject() (config: Configuration) extends Controller {

  def letsEncrypt(key: String) = Action {
    Ok(config.getString("letsencrypt.key").getOrElse(""))
  }

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

  def receiveMessage = Action { implicit request =>
    play.Logger.info(s"Request body: ${request.body}")
    play.Logger.info(s"Request body: ${request.body.asJson}")
    Ok("Be very welcome.")
  }
}
