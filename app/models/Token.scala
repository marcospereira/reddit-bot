package models

import play.api.libs.functional.syntax._
import play.api.libs.json.{ JsPath, Json, Reads, Writes }

case class Token(accessToken: String, tokenType: String, expiresIn: Long, scope: String)

object Token {

  implicit val tokenReads: Reads[Token] = (
    (JsPath \ "access_token").read[String] and
    (JsPath \ "token_type").read[String] and
    (JsPath \ "expires_in").read[Long] and
    (JsPath \ "scope").read[String]
  )(Token.apply _)

  implicit val tokenWrites: Writes[Token] = (
    (JsPath \ "access_token").write[String] and
    (JsPath \ "token_type").write[String] and
    (JsPath \ "expires_in").write[Long] and
    (JsPath \ "scope").write[String]
  )(unlift(Token.unapply))
}