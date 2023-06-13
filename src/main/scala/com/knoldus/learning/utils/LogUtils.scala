package com.knoldus.learning.utils

import com.typesafe.scalalogging.Logger


object LogUtils {
  def logSqlStatement(logger: Logger)(method: String, statements: Seq[String]): Unit = {
    logger.debug(
      s"""
         | \n\n\n===================================================================
         | Inside $method.\n Executing SQL Statement: ${statements.mkString(";")}
         |===================================================================\n\n\n
         | """.stripMargin)
  }
}
