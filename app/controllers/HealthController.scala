package controllers

import javax.inject.{ Inject, Singleton }

import play.api.cache.CacheApi
import play.api.libs.json.Json
import play.api.mvc.{ Action, Controller }

import scala.concurrent.duration._

case class HealthStatus(service: String, status: String)

object HealthStatus {

  def running(service: String) = HealthStatus(service, "Running")
  def failing(service: String) = HealthStatus(service, "Failing")

  implicit val healthStatusFormat = Json.format[HealthStatus]

}

@Singleton
class HealthController @Inject() (cacheApi: CacheApi) extends Controller {

  def index = Action {
    cacheApi.set("cache.health", true, 1.second)
    cacheApi.get[Boolean]("cache.health").map { status =>
      Ok(Json.toJson(HealthStatus.running("cache")))
    }.getOrElse {
      InternalServerError(Json.toJson(HealthStatus.failing("cache")))
    }.as("application/json; charset=utf-8")
  }
}
