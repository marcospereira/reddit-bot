package models

import play.api.libs.json.Json

case class User(id: Long)
case class Message(mid: Option[String] = None, seq: Option[Long] = None, text: String)
case class Delivery(watermark: Long, seq: Long)
case class Messaging(sender: User, recipient: User, timestamp: Option[Long] = None, message: Option[Message] = None, delivery: Option[Delivery])
case class Entry(id: Long, time: Long, messaging: Seq[Messaging])
case class PostedMessage(`object`: String, entry: Seq[Entry])

case class BotHelpResponse(recipient: User, message: Message)

case class Button(`type`: String = "web_url", title: String, url: String)
case class Card(title: String, subtitle: String, image_url: Option[String], buttons: Seq[Button])
case class Payload(template_type: String = "generic", elements: Seq[Card])
case class Attachment(`type`: String = "template", payload: Payload)

object Attachment {
  def from(posts: Seq[RedditPost]): Attachment = {
    val cards = posts.map { post =>
      Card(
        title = post.title,
        subtitle = s"From ${post.author} | ${post.numComments} comments | ${post.ups} ups | ${post.downs} downs",
        image_url = None,
        buttons = Seq(
          Button(title = "Open link", url = post.url)
        )
      )
    }
    Attachment(payload = Payload(elements = cards))
  }
}

object Messages {

  implicit val userFormat = Json.format[User]
  implicit val messageFormat = Json.format[Message]
  implicit val deliveryFormat = Json.format[Delivery]
  implicit val messagingFormat = Json.format[Messaging]
  implicit val entryFormat = Json.format[Entry]
  implicit val postedMessageFormat = Json.format[PostedMessage]

  implicit val messageResponseFormat = Json.format[BotHelpResponse]
  implicit val buttonFormat = Json.format[Button]
  implicit val cardFormat = Json.format[Card]
  implicit val payloadFormat = Json.format[Payload]
  implicit val attachmentFormat = Json.format[Attachment]

  lazy val commandFormat = "/?([a-zA-Z]+)/(hot|top)".r

  def help(sender: User) = BotHelpResponse(sender, message = Message(text =
    """
      | I'm a robot. You need to be very specific with me. Here is what you can say:
      | 1. help
      | 2. /subreddit/order where order is "hot" or "top"
      |
      | Some examples:
      | 1. /foot/hot
      | 2. /science/top
    """.stripMargin))
}