package services

import org.slf4j.LoggerFactory
import play.api.libs.ws.{ WSRequest, WSRequestExecutor, WSRequestFilter, WSResponse }

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.collection.immutable.Iterable

case class AhcResponseLogger(
    headers: Boolean = true,
    statusCode: Boolean = true,
    body: Boolean = false
) extends WSRequestFilter {

  private val logger = LoggerFactory.getLogger(classOf[AhcResponseLogger])

  override def apply(executor: WSRequestExecutor): WSRequestExecutor = {
    new WSRequestExecutor {
      override def execute(request: WSRequest): Future[WSResponse] = {
        val futureResponse: Future[WSResponse] = executor.execute(request)
        futureResponse.map { response =>
          logStatusCode(response)
          logHeaders(response)
          logBody(response)
          response
        }
      }

      private def logStatusCode(response: WSResponse) = if (statusCode) {
        logger.info(s"${response.status} ${response.statusText}")
      }

      private def logHeaders(response: WSResponse) = if (headers) {
        val formattedHeaders: Iterable[String] = response.allHeaders.map(header => s"${header._1}: ${header._2.mkString(",")}")
        logger.info(formattedHeaders.mkString(start = "\n", sep = "\n", end = "\n"))
      }

      private def logBody(response: WSResponse) = if (body) {
        logger.info(s"${response.body}")
      }
    }
  }
}
