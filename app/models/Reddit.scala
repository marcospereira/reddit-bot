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

//case class RedditPostPreview(url: String, width: Long, height: Long)

case class RedditPost(
  id: String,
  url: String,
  title: String,
  domain: String,
  subreddit: String,
  author: String,
  permalink: String,
  score: Long,
  over18: Boolean,
  hidden: Boolean,
  //  previewSource: Option[RedditPostPreview],
  //  previewResolutions: Option[Seq[RedditPostPreview]],
  numComments: Long,
  downs: Long,
  ups: Long,
  created: Long
)

object RedditPost {

  //  implicit val redditPostPreviewReads = Json.reads[RedditPostPreview]

  implicit val redditPostReads: Reads[RedditPost] = (
    (JsPath \ "data" \ "id").read[String] and
    (JsPath \ "data" \ "url").read[String] and
    (JsPath \ "data" \ "title").read[String] and
    (JsPath \ "data" \ "domain").read[String] and
    (JsPath \ "data" \ "subreddit").read[String] and
    (JsPath \ "data" \ "author").read[String] and
    (JsPath \ "data" \ "permalink").read[String] and
    (JsPath \ "data" \ "score").read[Long] and
    (JsPath \ "data" \ "over_18").read[Boolean] and
    (JsPath \ "data" \ "hidden").read[Boolean] and
    //    (JsPath \ "data" \ "preview" \ "images" \ "source").readNullable[RedditPostPreview] and
    //    (JsPath \ "data" \ "preview" \ "images" \ "resolutions").readNullable[Seq[RedditPostPreview]] and
    (JsPath \ "data" \ "num_comments").read[Long] and
    (JsPath \ "data" \ "downs").read[Long] and
    (JsPath \ "data" \ "ups").read[Long] and
    (JsPath \ "data" \ "created_utc").read[Long]
  )(RedditPost.apply _)

  //  implicit val redditPostPreviewWriters = Json.writes[RedditPostPreview]

  implicit val redditPostWriters = Json.writes[RedditPost]
}