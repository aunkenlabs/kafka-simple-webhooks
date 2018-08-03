package com.graphpathai.utils

import akka.actor.ActorSystem
import akka.pattern.after
import com.graphpathai.utils.DurationOps._
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration.{FiniteDuration, _}
import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds
import scala.util.Random

object FutureOps extends StrictLogging {

  def expBackoff[A](maxExecutions: Int, baseWait: FiniteDuration, maxWait: FiniteDuration)(f: => Future[A])
                   (implicit ec: ExecutionContext, system: ActorSystem): Future[A] = {
    def loop(attempt: Int): Future[A] = f.recoverWith {
      case e if attempt <= maxExecutions =>
        logger.warn(s"Failure on attempt $attempt, retrying...", e)

        val maxWaitMillis = math.min(maxWait.toMillis, baseWait.toMillis * math.pow(2, attempt - 1))
        val waitMillis = baseWait.toMillis + Random.nextInt(maxWaitMillis.toInt)

        after(waitMillis.millis, system.scheduler)(loop(attempt + 1))
      case e => Future.failed(e)
    }

    loop(1)
  }

  trait Retry {
    def retry[A](f: => Future[A])(implicit config: Config, ec: ExecutionContext, system: ActorSystem): Future[A] =
      expBackoff(
        config.getInt("retry.maxExecutions"),
        config.getDuration("retry.baseWait"),
        config.getDuration("retry.maxWait")
      )(f)
  }

}
