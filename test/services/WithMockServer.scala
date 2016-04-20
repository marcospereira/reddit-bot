package services

import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.routing.Router
import play.api.test.{ Helpers, TestServer }

trait WithMockServer {

  def withMockServer(router: Router)(block: (Application => Unit)) {
    val server: TestServer = testServer(router)
    Helpers.running(server) {
      block(server.application)
    }
  }

  // the mock server
  def testServer(router: Router): TestServer = {
    val port = 9999
    val app = GuiceApplicationBuilder()
      .configure(
        "reddit.tokenUrl" -> s"http://localhost:$port/token",
        "reddit.api.clientId" -> "Client-Id",
        "reddit.api.secret" -> "Secret"
      )
      .router(router)
      .build()
    TestServer(port, app)
  }
}
