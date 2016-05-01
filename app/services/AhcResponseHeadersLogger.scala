package services

import org.slf4j.LoggerFactory
import play.api.libs.ws.{ WSRequest, WSRequestExecutor, WSRequestFilter, WSResponse }

import scala.concurrent.Future

import play.api.libs.concurrent.Execution.Implicits.defaultContext

case class AhcResponseHeadersLogger() extends WSRequestFilter {

  private val logger = LoggerFactory.getLogger(classOf[AhcResponseHeadersLogger])

  override def apply(executor: WSRequestExecutor): WSRequestExecutor = {
    new WSRequestExecutor {
      override def execute(request: WSRequest): Future[WSResponse] = {
        val futureResponse: Future[WSResponse] = executor.execute(request)
        futureResponse.map { response =>
          logger.info(response.allHeaders.map(header => s"${header._1}: ${header._2.mkString(",")}").mkString("\n"))
          response
        }
      }
    }
  }
}
