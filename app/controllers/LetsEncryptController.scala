package controllers

import javax.inject.{ Inject, Singleton }

import play.api.Configuration
import play.api.mvc.Action
import play.api.mvc.Controller

@Singleton
class LetsEncryptController @Inject() (config: Configuration) extends Controller {

  def index(key: String) = Action {
    Ok(config.getString("letsencrypt.key").getOrElse(""))
  }
}
