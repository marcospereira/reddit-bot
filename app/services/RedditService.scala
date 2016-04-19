package services

import java.nio.charset.StandardCharsets.UTF_8
import javax.inject.{ Inject, Singleton }

import org.asynchttpclient.util.Base64
import play.api.Configuration
import play.api.cache.{ CacheApi, NamedCache }
import play.api.http.HeaderNames._
import play.api.libs.ws.WSClient

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import models._
import models.Token._
import play.api.libs.json._

import scala.concurrent.duration._

@Singleton
class RedditClient @Inject() (
    ws: WSClient,
    config: Configuration,
    @NamedCache("tokens-cache") tokensCache: CacheApi,
    @NamedCache("reddit-cache") redditCache: CacheApi
) {

  lazy val tokenUrl: String = getConfig("reddit.tokenUrl")
  lazy val userAgent: String = getConfig("reddit.userAgent")

  lazy val baseUrl: String = getConfig("reddit.api.baseUrl")
  lazy val clientId: String = getConfig("reddit.api.clientId")
  lazy val secretId: String = getConfig("reddit.api.secret")

  lazy val base64Auth = Base64.encode(s"$clientId:$secretId".getBytes(UTF_8))

  private def getConfig(key: String): String = {
    config.getString(key).getOrElse(sys.error(s"Configuration $key was not found"))
  }

  def get(resource: String): Future[Seq[RedditPost]] = {
    getToken flatMap { token => getResource(token, resource) }
  }

  // Get oAuth2 token
  private def getToken: Future[Token] = {
    val cacheKey = "token.cache"
    tokensCache.get[Token](cacheKey)
      .map(token => Future(token))
      .getOrElse {
        val result = ws.url(tokenUrl)
          .withHeaders(AUTHORIZATION -> s"Basic $base64Auth")
          // .withRequestFilter(play.api.libs.ws.ahc.AhcCurlRequestLogger())
          .post(Map("grant_type" -> Seq("client_credentials")))
          .map(response => response.json.as[Token])
        result.onSuccess {
          case token => tokensCache.set(cacheKey, token, token.expiresIn.seconds - 5.seconds) // renew the token a little earlier
        }
        result
      }
  }

  private def getResource(token: Token, resource: String): Future[Seq[RedditPost]] = {
    ws.url(s"$baseUrl/$resource")
      .withHeaders(
        AUTHORIZATION -> s"Bearer ${token.accessToken}",
        USER_AGENT -> userAgent
      )
      // .withRequestFilter(play.api.libs.ws.ahc.AhcCurlRequestLogger())
      .get()
      .map(response => response.json.transform((__ \ 'data \ 'children).json.pick).get)
      .map(json => json.as[Seq[RedditPost]])
  }
}

@Singleton
class RedditService @Inject() (client: RedditClient, @NamedCache("reddit-cache") redditCache: CacheApi) {

  def hot(subreddit: String, limit: Option[Int] = Some(25)): Future[Seq[RedditPost]] =
    getSubreddit(subreddit, "hot", limit)

  def newest(subreddit: String, limit: Option[Int] = Some(25)): Future[Seq[RedditPost]] =
    getSubreddit(subreddit, "new", limit)

  def top(subreddit: String, limit: Option[Int] = Some(25)): Future[Seq[RedditPost]] =
    getSubreddit(subreddit, "top", limit)

  def controversial(subreddit: String, limit: Option[Int] = Some(25)): Future[Seq[RedditPost]] =
    getSubreddit(subreddit, "random", limit)

  private def getSubreddit(subreddit: String, order: String, limit: Option[Int]): Future[Seq[RedditPost]] = {
    val resource = s"""r/$subreddit/$order${limit.map("?limit=" + _).getOrElse("")}"""
    redditCache.get[Seq[RedditPost]](resource) match {
      case Some(posts) =>
        Future(posts)
      case None =>
        val future = client.get(resource)
        future.onSuccess { case posts => redditCache.set(resource, posts, 1.minute) }
        future
    }
  }
}