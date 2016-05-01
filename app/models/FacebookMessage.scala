package models

import com.github.jreddit.parser.entity.Submission
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
  def from(posts: Seq[Submission]): Attachment = {
    val cards = posts.take(10).map { post =>
      Card(
        title = post.getTitle,
        subtitle = s"From ${post.getAuthor} | ${post.getCommentCount} comments | ${post.getUpVotes} ups | ${post.getDownVotes} downs",
        image_url = if (post.getSource != null) Some(post.getSource.getUrl) else None,
        buttons = buttons(post)
      )
    }
    Attachment(payload = Payload(elements = cards))
  }

  private def buttons(post: Submission): Seq[Button] = {
    val buttons = Seq[Button](Button(title = "Open link", url = post.getURL))
    if (post.getURL.contains("reddit.com/r/"))
      buttons
    else
      buttons :+ Button(title = "Reddit conversation", url = "https://www.reddit.com" + post.getPermalink)
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

  lazy val commandFormat = "/?([a-zA-Z0-9_]+)/(hot|top)".r

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