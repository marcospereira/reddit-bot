package controllers

import javax.inject._

import play.api.mvc._
import play.libs.Json
import services.RedditService

import scala.collection.JavaConversions
import scala.concurrent.ExecutionContext

@Singleton
class RedditController @Inject() (redditService: RedditService)(implicit exec: ExecutionContext) extends Controller {

  def hot(subreddit: String, limit: Option[Int]) = Action.async {
    redditService.getSubreddit(subreddit, "hot", limit) map { posts =>
      Ok(Json.toJson(JavaConversions.seqAsJavaList(posts)).toString).as("application/json; charset=utf-8")
    }
  }

  def newest(subreddit: String, limit: Option[Int]) = Action.async {
    redditService.getSubreddit(subreddit, "new", limit) map { posts =>
      Ok(Json.toJson(JavaConversions.seqAsJavaList(posts)).toString).as("application/json; charset=utf-8")
    }
  }

  def top(subreddit: String, limit: Option[Int]) = Action.async {
    redditService.getSubreddit(subreddit, "top", limit) map { posts =>
      Ok(Json.toJson(JavaConversions.seqAsJavaList(posts)).toString).as("application/json; charset=utf-8")
    }
  }

  def controversial(subreddit: String, limit: Option[Int]) = Action.async {
    redditService.getSubreddit(subreddit, "controversial", limit) map { posts =>
      Ok(Json.toJson(JavaConversions.seqAsJavaList(posts)).toString).as("application/json; charset=utf-8")
    }
  }
}
