package com.graphpathai.ws

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.google.inject.{AbstractModule, Provides}
import com.typesafe.config.Config
import javax.inject.Singleton
import play.api.libs.ws.ahc.{AhcWSClientConfigFactory, StandaloneAhcWSClient}

object WsModule extends AbstractModule {
  override def configure(): Unit = ()

  @Provides
  @Singleton
  def actorSystemProvider(): ActorSystem = ActorSystem()

  @Provides
  @Singleton
  def clientProvider(config: Config)(implicit actorSystem: ActorSystem): StandaloneAhcWSClient = {
    StandaloneAhcWSClient(config = AhcWSClientConfigFactory.forConfig(config))(ActorMaterializer())
  }
}
