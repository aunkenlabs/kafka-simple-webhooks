package com.graphpathai.ws

import akka.actor.ActorSystem
import com.graphpathai.utils.DurationOps._
import com.graphpathai.utils.FutureOps.Retry
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.DefaultBodyWritables._
import play.api.libs.ws.StandaloneWSResponse
import play.api.libs.ws.WSAuthScheme.BASIC
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.collection.JavaConverters._
import scala.concurrent.{Await, ExecutionContext, Future}

@Singleton
class WebHookCaller @Inject()(ws: StandaloneAhcWSClient, config: Config)
                             (implicit ec: ExecutionContext, actorSystem: ActorSystem)
  extends Retry with StrictLogging {

  private implicit val webHookConfig: Config = config.getConfig("webhook")
  private val url = webHookConfig.getString("url")
  private val method = webHookConfig.getString("method")
  private val headers = webHookConfig.getObject("headers")
    .unwrapped().asScala
    .map { case (k, v) => k -> v.toString }
    .toSeq
  private val timeout = webHookConfig.getDuration("retry.timeout")
  private val basicAuth =
    if (webHookConfig.getBoolean("basicAuth.enabled"))
      Some(
        webHookConfig.getString("basicAuth.username") ->
          webHookConfig.getString("basicAuth.password")
      )
    else
      None

  def syncExecute(v: Array[Byte]): StandaloneWSResponse =
    Await.result(execute(v), timeout)

  def execute(v: Array[Byte]): Future[StandaloneWSResponse] = retry {
    logger.info(s"$method $url - Headers: ${headers.map { case (h, hv) => s"$h=$hv" }.mkString(", ")}")
    val request = ws.url(url)
      .addHttpHeaders(headers: _*)
      .withMethod(method)
      .withBody(v)

    val requestWithAuth = basicAuth
      .map {
        case (username, password) => request.withAuth(username, password, BASIC)
      }
      .getOrElse(request)

    requestWithAuth
      .execute()
      .map(only2xx)
  }

  private def only2xx(response: StandaloneWSResponse): StandaloneWSResponse =
    if ((200 until 300).contains(response.status))
      response
    else
      throw WsException(response)

  def close(): Unit = {
    ws.close()
    actorSystem.terminate()
  }

}
