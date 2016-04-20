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
    redditService.getSubreddit(subreddit, "hot", limit) map { posts =>
      Ok(Json.toJson(posts)).as("application/json; charset=utf-8")
    }
  }

  def newest(subreddit: String, limit: Option[Int]) = Action.async {
    redditService.getSubreddit(subreddit, "new", limit) map { posts =>
      Ok(Json.toJson(posts)).as("application/json; charset=utf-8")
    }
  }

  def top(subreddit: String, limit: Option[Int]) = Action.async {
    redditService.getSubreddit(subreddit, "top", limit) map { posts =>
      Ok(Json.toJson(posts)).as("application/json; charset=utf-8")
    }
  }

  def controversial(subreddit: String, limit: Option[Int]) = Action.async {
    redditService.getSubreddit(subreddit, "controversial", limit) map { posts =>
      Ok(Json.toJson(posts)).as("application/json; charset=utf-8")
    }
  }
}
