package com.graphpathai

import com.google.inject.{AbstractModule, Guice}
import com.graphpathai.ws.WsModule
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.ExecutionContext

object Main {

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.systemEnvironment().withFallback(ConfigFactory.load())
    val injector = Guice.createInjector(new MainModule(config), WsModule)
    val consumer = injector.getInstance(classOf[Stream])

    consumer.start()

    // on shutdown close the stream
    sys.addShutdownHook(consumer.stop())
  }

  class MainModule(config: Config) extends AbstractModule {
    override def configure(): Unit = {
      bind(classOf[Config]).toInstance(config)
      bind(classOf[ExecutionContext]).toInstance(scala.concurrent.ExecutionContext.global)
    }
  }

}
