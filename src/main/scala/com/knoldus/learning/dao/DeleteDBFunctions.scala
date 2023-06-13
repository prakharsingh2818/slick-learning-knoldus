package com.knoldus.learning.dao

import com.knoldus.learning.tables.StudentTableApi
import com.knoldus.learning.utils.DbUtils.execute
import com.knoldus.learning.utils.LogUtils.logSqlStatement
import com.typesafe.scalalogging.Logger
import slick.jdbc.PostgresProfile.api._

object DeleteDBFunctions {
  private val baseLogger = Logger(getClass.getName)
  private val log = logSqlStatement(baseLogger) _

  def deleteByName(name: String): Int = {
    val deleteByName = StudentTableApi.studentTable.filter(_.name === name).delete
    log("deleteByName", deleteByName.statements.toSeq)
    val executeDeleteByName = execute(deleteByName)
    executeDeleteByName
  }

}
