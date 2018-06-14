/*
 * Copyright 2018 Greg Methvin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.methvin.logback

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, RequestEntity, Uri}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorMaterializer, Materializer}
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.{Layout, LayoutBase, UnsynchronizedAppenderBase}
import com.typesafe.config.{Config, ConfigFactory}
import spray.json.{DefaultJsonProtocol, JsValue}

import scala.collection.immutable
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

private object SlackAppender {
  case class SlackPostMessage(
    channel: String,
    text: String,
    color: Option[String] = None,
    username: Option[String] = None,
    icon_emoji: Option[String] = None,
    mrkdwn: Boolean = false
  )
  object SlackPostMessage {
    import DefaultJsonProtocol._
    implicit val format = jsonFormat6(SlackPostMessage.apply)
  }

  val DefaultLayout = new LayoutBase[ILoggingEvent] {
    override def doLayout(e: ILoggingEvent): String = {
      s"[${e.getLevel}] ${e.getLoggerName} - ${e.getFormattedMessage.replaceAll("\n", "\n\t")}"
    }
  }
  val DefaultUri = "https://slack.com/api/chat.postMessage"
}

class SlackAppender extends UnsynchronizedAppenderBase[ILoggingEvent] {

  import SlackAppender._

  implicit private val akkaConfig: Config = ConfigFactory.parseString("""
      | akka.http.parsing.illegal-header-warnings = off
    """.stripMargin).resolve()
  implicit private val system: ActorSystem = ActorSystem("slack-appender", akkaConfig)
  implicit private val materializer: Materializer = ActorMaterializer()
  implicit private val executionContext: ExecutionContext = system.dispatcher

  private var uri: String = DefaultUri
  private var channel: Option[String] = None
  private var username: Option[String] = None
  private var iconEmoji: Option[String] = None
  private var layout: Layout[ILoggingEvent] = DefaultLayout
  private var token: String = ""

  def getUri: String = this.uri

  def setUri(url: String): Unit = {
    this.uri = Option(url).getOrElse(DefaultUri)
  }

  def getToken: String = this.token

  def setToken(token: String): Unit = {
    this.token = token
  }

  def getChannel: String = this.channel.orNull

  def setChannel(channel: String): Unit = {
    this.channel = Option(channel)
  }

  def getUsername: String = this.username.orNull

  def setUsername(username: String): Unit = {
    this.username = Option(username)
  }

  def getIconEmoji: String = this.iconEmoji.orNull

  def setIconEmoji(iconEmoji: String): Unit = {
    this.iconEmoji = Option(iconEmoji)
  }

  def getLayout: Layout[ILoggingEvent] = this.layout

  def setLayout(layout: Layout[ILoggingEvent]): Unit = {
    this.layout = Option(layout).getOrElse(DefaultLayout)
  }

  override def append(event: ILoggingEvent): Unit = {
    val channels = Option(event.getMarker)
      .map(_.getName)
      .filter(c => c.startsWith("#") || c.startsWith("@"))
      .toSeq ++ this.channel

    channels.foreach(postToSlack(_, event))
  }

  private def postToSlack(channel: String, event: ILoggingEvent): Unit = {
    val payload = SlackPostMessage(channel, layout.doLayout(event), username, iconEmoji)

    val responseFuture = Marshal(payload).to[RequestEntity].flatMap { entity =>
      Http().singleRequest(
        HttpRequest(HttpMethods.POST, Uri(uri), immutable.Seq(Authorization(OAuth2BearerToken(token))), entity)
      )
    }

    responseFuture.onComplete {
      case Success(response) =>
        Unmarshal(response.entity).to[JsValue].foreach { value =>
          if (value.asJsObject.getFields("error").nonEmpty) {
            system.log.warning(s"[SlackAppender] got error from Slack: $value")
          }
        }
      case Failure(e) =>
        system.log.error(s"[SlackAppender] got exception logging to Slack!", e)
    }
  }
}
