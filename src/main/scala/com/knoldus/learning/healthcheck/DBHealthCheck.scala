package com.knoldus.learning.healthcheck

import slick.ast.ScalaBaseType.intType
import slick.lifted.Query

object DBHealthCheck {

  def ping() = {
    println("Checking DB Health")
    Query(1)
  }
}
