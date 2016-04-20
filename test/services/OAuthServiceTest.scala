package services

import models.Token
import org.scalatestplus.play.PlaySpec
import play.api.Application
import play.api.libs.json.Json
import play.api.mvc.{ Action, Results }
import play.api.routing.Router

import scala.concurrent.Await
import scala.concurrent.duration._

class OAuthServiceTest extends PlaySpec with WithMockServer {

  val jsonToken = Json.parse(
    """
      |{
      |    "access_token": "TheAccessToken",
      |    "token_type": "bearer",
      |    "expires_in": 3600,
      |    "scope": "*"
      |}
    """.stripMargin
  )

  "OAuthService" must {
    "get a new token" in {
      withOAuthServer { app =>
        val authService = app.injector.instanceOf[OAuthService]
        val result = Await.result(authService.getToken, 2.seconds)
        result.accessToken mustBe "TheAccessToken"
      }
    }
    "caches the received token" in {
      withOAuthServer { app =>
        val authService = app.injector.instanceOf[OAuthService]
        Await.result(authService.getToken, 2.seconds)
        authService.getCache.get[Token]("token.cache").value.accessToken mustBe "TheAccessToken"
      }
    }
  }

  private def withOAuthServer(block: Application => Unit) = {
    val router = Router.from {
      case _ => Action { Results.Ok(jsonToken) }
    }
    withMockServer(router)(block)
  }
}
