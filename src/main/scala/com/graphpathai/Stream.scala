package com.graphpathai

import java.util.Properties
import java.util.concurrent.TimeUnit

import com.graphpathai.ws.WebHookCaller
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import javax.inject.{Inject, Singleton}
import org.apache.kafka.streams.KafkaStreams.State
import org.apache.kafka.streams.{KafkaStreams, StreamsBuilder}

@Singleton
class Stream @Inject()(wh: WebHookCaller)(implicit config: Config) extends StrictLogging {

  private val kafkaConfig = config.getConfig("kafka")
  private val topic = kafkaConfig.getString("topic")
  private val topology = {
    val builder = new StreamsBuilder
    builder
      .stream[Array[Byte], Array[Byte]](topic)
      .foreach((k, v) => wh.syncExecute(v))
    builder.build()
  }
  private val streamingConfig = kafkaConfig.toProperties
  private val stream = new KafkaStreams(topology, streamingConfig)

  stream.setUncaughtExceptionHandler { (t, e) =>
    logger.error("Uncaught exception on kafka streams", e)
  }

  stream.setStateListener((newState, _) => {
    newState match {
      case State.ERROR => stop()
      case _ => // do nothing
    }
  })

  def start(): Unit = {
    logger.info("Starting kafka stream...")
    stream.start()
  }

  def stop(): Unit = {
    logger.info("Stoping kafka stream...")
    // timeout to avoid deadlock
    stream.close(5, TimeUnit.SECONDS)
    wh.close()
  }

  implicit class ConfigAdapter(config: Config) {

    def toProperties: Properties = {
      val properties = new Properties()
      config.entrySet()
        .forEach(e =>
          properties.setProperty(e.getKey, config.getString(e.getKey))
        )
      properties
    }

  }

}
