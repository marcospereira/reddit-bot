package services

import javax.inject.{ Inject, Singleton }

import com.github.jreddit.parser.entity.Submission
import com.github.jreddit.parser.listing.SubmissionsListingParser
import play.api.Configuration
import play.api.cache.{ CacheApi, NamedCache }
import play.api.http.HeaderNames._
import play.api.libs.json._
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

import scala.collection.JavaConversions._

@Singleton
class RedditService @Inject() (
    ws: WSClient,
    config: Configuration,
    oAuthService: OAuthService,
    redditParser: SubmissionsListingParser,
    @NamedCache("reddit-cache") redditCache: CacheApi
) {

  private lazy val userAgent = getConfig("reddit.userAgent")
  private lazy val baseUrl = getConfig("reddit.api.baseUrl")

  private def getConfig(key: String): String = {
    config.getString(key).getOrElse(sys.error(s"Configuration $key was not found"))
  }

  private def get(resource: String): Future[Seq[Submission]] = {
    oAuthService.getToken flatMap { token =>
      ws.url(s"$baseUrl/$resource")
        .withHeaders(
          AUTHORIZATION -> s"Bearer ${token.accessToken}",
          USER_AGENT -> userAgent
        )
        .withRequestFilter(play.api.libs.ws.ahc.AhcCurlRequestLogger()) // TODO remove
        .withRequestFilter(AhcResponseLogger()) // TODO remove
        .get()
        .map(response => redditParser.parse(response.body))
    }
  }

  def getSubreddit(subreddit: String, order: String, limit: Option[Int] = None): Future[Seq[Submission]] = {
    val resource = s"""r/$subreddit/$order${limit.map("?limit=" + _).getOrElse("")}"""
    redditCache.get[Seq[Submission]](resource) match {
      case Some(posts) =>
        Future(posts)
      case None =>
        val future = get(resource)
        future.onSuccess { case posts => redditCache.set(resource, posts, 1.minute) }
        future
    }
  }
}