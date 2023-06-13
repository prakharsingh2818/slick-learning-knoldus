package com.knoldus.learning.utils

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.{DurationInt, FiniteDuration}


object Utils {
  private val DefaultTimeout = 10.seconds
  def await[T](f: => Future[T])(implicit timeout: FiniteDuration = DefaultTimeout): T = Await.result(f, timeout)
}
