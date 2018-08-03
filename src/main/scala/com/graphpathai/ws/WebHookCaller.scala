package com.graphpathai.ws

import akka.actor.ActorSystem
import com.graphpathai.utils.DurationOps._
import com.graphpathai.utils.FutureOps.Retry
import com.typesafe.config.Config
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.DefaultBodyWritables._
import play.api.libs.ws.StandaloneWSResponse
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.collection.JavaConverters._
import scala.concurrent.{Await, ExecutionContext, Future}

@Singleton
class WebHookCaller @Inject()(ws: StandaloneAhcWSClient, config: Config)
                             (implicit ec: ExecutionContext, actorSystem: ActorSystem)
  extends Retry {

  private implicit val webHookConfig: Config = config.getConfig("webhook")
  private val url = webHookConfig.getString("url")
  private val method = webHookConfig.getString("method")
  private val headers = webHookConfig.getObject("headers")
    .unwrapped().asScala
    .map { case (k, v) => k -> v.toString }
    .toSeq
  private val timeout = webHookConfig.getDuration("timeout")

  def syncExecute(v: Array[Byte]): StandaloneWSResponse =
    Await.result(execute(v), timeout)

  def execute(v: Array[Byte]): Future[StandaloneWSResponse] = retry {
    ws.url(url)
      .addHttpHeaders(headers: _*)
      .withMethod(method)
      .withBody(v)
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
