import java.nio.charset.StandardCharsets._
import javax.inject.{ Inject, Singleton }

import models.Token
import org.asynchttpclient.util.Base64
import play.api.Configuration
import play.api.cache.{ CacheApi, _ }
import play.api.http.HeaderNames._
import play.api.libs.ws.WSClient

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._

package services {
  @Singleton
  class OAuthService @Inject() (
      ws: WSClient,
      config: Configuration,
      @NamedCache("tokens-cache") tokensCache: CacheApi
  )(implicit executionContext: ExecutionContext) {

    lazy val tokenUrl = getConfig("reddit.tokenUrl")
    lazy val clientId = getConfig("reddit.api.clientId")
    lazy val secretId = getConfig("reddit.api.secret")

    lazy val base64Auth = Base64.encode(s"$clientId:$secretId".getBytes(UTF_8))

    private def getConfig(key: String): String = {
      config.getString(key).getOrElse(sys.error(s"Configuration $key was not found"))
    }

    def getToken: Future[Token] = {
      val cacheKey = "token.cache"
      tokensCache.get[Token](cacheKey)
        .map(token => Future(token))
        .getOrElse {
          val result = ws.url(tokenUrl)
            .withHeaders(AUTHORIZATION -> s"Basic $base64Auth")
            .withRequestFilter(play.api.libs.ws.ahc.AhcCurlRequestLogger()) // TODO remove
            .withRequestFilter(AhcResponseLogger()) // TODO remove
            .post(Map("grant_type" -> Seq("client_credentials")))
            .map(response => response.json.as[Token])
          result.onSuccess {
            case token => tokensCache.set(cacheKey, token, token.expiresIn.seconds - 5.seconds) // renew the token a little earlier
          }
          result
        }
    }

    def getCache = tokensCache
  }
}

