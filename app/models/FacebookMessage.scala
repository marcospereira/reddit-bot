package models

import com.github.jreddit.parser.entity.Submission
import play.api.libs.json.Json

case class User(id: Long)
case class Message(mid: Option[String] = None, seq: Option[Long] = None, text: String)
case class Delivery(watermark: Long, seq: Long)

case class Messaging(
  sender: User,
  recipient: User,
  timestamp: Option[Long] = None,
  message: Option[Message] = None,
  delivery: Option[Delivery] = None
)

case class Entry(id: Long, time: Long, messaging: Seq[Messaging])

/**
 * Represents a message received from Messenger Platform. It has the following structure:
 *
 * {{{
 *   {
 *     "object":"page",
 *     "entry":[
 *       {
 *         "id":"PAGE_ID",
 *         "time":1460245674269,
 *         "messaging":[
 *           {
 *             "sender":{
 *               "id":"USER_ID"
 *             },
 *             "recipient":{
 *               "id":"PAGE_ID"
 *             },
 *             "timestamp":1460245672080,
 *             "message":{
 *               "mid":"mid.1460245671959:dad2ec9421b03d6f78",
 *               "seq":216,
 *               "text":"hello"
 *             }
 *           }
 *         ]
 *       }
 *     ]
 *   }
 * }}}
 *
 * @param `object` the facebook object from where the message is being sent
 * @param entry the messages
 */
case class ReceivedMessage(`object`: String, entry: Seq[Entry])

/**
 * A simple text message that follow the structure below:
 *
 * {{{
 *   {
 *     "recipient": {
 *       "id": 12345
 *     },
 *     "message": {
 *        "text": "The message text"
 *     }
 *   }
 * }}}
 *
 * This is used to send very simple messages to the user, such as greetings and
 * also instructions about how to use the bot.
 *
 * @param recipient the user that will receive the message
 * @param message the message itself
 */
case class TextResponse(recipient: User, message: Message)

case class Button(`type`: String = "web_url", title: String, url: String)
case class Card(title: String, subtitle: String, image_url: Option[String], buttons: Seq[Button])
case class Payload(template_type: String = "generic", elements: Seq[Card])
case class Attachment(`type`: String = "template", payload: Payload)

/**
 * The structured message to send rich content like bubbles/cards. It has the following structure:
 *
 * {{{
 *   {
 *     "recipient": {
 *       "id": "USER_ID"
 *     },
 *     "message": {
 *         "attachment": {
 *             "type": "template",
 *             "payload": {
 *                 "template_type": "generic",
 *                 "elements": [
 *                     {
 *                         "title": "First card",
 *                         "subtitle": "Element #1 of an hscroll",
 *                         "image_url": "http://messengerdemo.parseapp.com/img/rift.png",
 *                         "buttons": [
 *                             {
 *                                 "type": "web_url",
 *                                 "url": "https://www.messenger.com/",
 *                                 "title": "Web url"
 *                             },
 *                             {
 *                                 "type": "postback",
 *                                 "title": "Postback",
 *                                 "payload": "Payload for first element in a generic bubble"
 *                             }
 *                         ]
 *                     },
 *                     {
 *                         "title": "Second card",
 *                         "subtitle": "Element #2 of an hscroll",
 *                         "image_url": "http://messengerdemo.parseapp.com/img/gearvr.png",
 *                         "buttons": [
 *                             {
 *                                 "type": "postback",
 *                                 "title": "Postback",
 *                                 "payload": "Payload for second element in a generic bubble"
 *                             }
 *                         ]
 *                     }
 *                 ]
 *             }
 *         }
 *     }
 *   }
 * }}}
 *
 * @param recipient the user that will receive the message
 * @param message the message with attachment
 */
case class StructuredMessage(recipient: User, message: Map[String, Attachment])

object Attachment {
  def from(posts: Seq[Submission]): Attachment = {
    val cards = posts.take(10).map { post =>
      Card(
        title = post.getTitle,
        subtitle = s"From ${post.getAuthor} | ${post.getCommentCount} comments | ${post.getUpVotes} ups | ${post.getDownVotes} downs",
        image_url = (if (post.getSource != null) Some(post.getSource.getUrl) else None).map(_.replaceAll("amp;", "")),
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
  implicit val receivedMessageFormat = Json.format[ReceivedMessage]

  implicit val textResponseFormat = Json.format[TextResponse]
  implicit val buttonFormat = Json.format[Button]
  implicit val cardFormat = Json.format[Card]
  implicit val payloadFormat = Json.format[Payload]
  implicit val attachmentFormat = Json.format[Attachment]
  implicit val structuredMessageFormat = Json.format[StructuredMessage]

  lazy val commandPattern = "/?([a-zA-Z0-9_]+)/(hot|top|new|controversial|rising)".r

  def help(sender: User) = TextResponse(sender, message = Message(text =
    """
      | I'm a robot. You need to be very specific with me. Here is what you can say:
      | 1. help
      | 2. /subreddit/order where order is "hot", "top", "new", "controversial" or "rising".
      |
      | Some examples:
      | 1. /food/hot
      | 2. /science/top
    """.stripMargin))

  def oops(sender: User) = TextResponse(sender, message = Message(text =
    """
    | You know, robots fails sometimes. Unfortunately I was not able to get
    | reddit posts. :-(
    |
    | You can try later.
  """.stripMargin))
}