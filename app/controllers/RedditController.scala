package controllers

import javax.inject._

import play.api.libs.json.Json
import play.api.mvc._
import services.RedditService

import scala.concurrent.ExecutionContext

import models.RedditPost._

@Singleton
class RedditController @Inject() (redditService: RedditService)(implicit exec: ExecutionContext) extends Controller {

  def hot(subreddit: String, limit: Option[Int]) = Action.async {
    redditService.hot(subreddit, limit) map { posts =>
      Ok(Json.toJson(posts)).as("application/json; charset=utf-8")
    }
  }

  def newest(subreddit: String, limit: Option[Int]) = Action.async {
    redditService.newest(subreddit, limit) map { posts =>
      Ok(Json.toJson(posts)).as("application/json; charset=utf-8")
    }
  }

  def top(subreddit: String, limit: Option[Int]) = Action.async {
    redditService.top(subreddit, limit) map { posts =>
      Ok(Json.toJson(posts)).as("application/json; charset=utf-8")
    }
  }

  def controversial(subreddit: String, limit: Option[Int]) = Action.async {
    redditService.controversial(subreddit, limit) map { posts =>
      Ok(Json.toJson(posts)).as("application/json; charset=utf-8")
    }
  }
}
