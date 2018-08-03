package com.graphpathai.utils

import scala.concurrent.duration.FiniteDuration
import scala.language.implicitConversions

object DurationOps {
  implicit def asFiniteDuration(d: java.time.Duration): FiniteDuration =
    scala.concurrent.duration.Duration.fromNanos(d.toNanos)
}