package com.knoldus.learning.dao

import com.knoldus.learning.model.{Student, StudentForm}
import com.knoldus.learning.tables.{StudentTable, StudentTableApi}
import com.knoldus.learning.utils.DbUtils.execute
import com.knoldus.learning.utils.LogUtils.logSqlStatement
import com.typesafe.scalalogging.Logger
import slick.dbio.Effect
import slick.jdbc.PostgresProfile.api._
import slick.lifted.MappedProjection
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.util.Random

object UpdateDBFunctions {
  private val baseLogger = Logger(getClass.getName)
  private val log = logSqlStatement(baseLogger) _

  def updateSingleField(name: String): Int = {
    // notice data type change before and after map!
    val selectQueryV1: Query[Rep[String], String, Seq] = StudentTableApi.studentTable.filter(_.name === name).map(_.`class`)
    val selectQueryV2: Query[StudentTable, Student, Seq] = StudentTableApi.studentTable.filter(_.name === name)

    val selectActionV1: FixedSqlStreamingAction[Seq[String], String, Effect.Read] = selectQueryV1.result
    val selectActionV2: FixedSqlStreamingAction[Seq[Student], Student, Effect.Read] = selectQueryV2.result
    val updateAction: FixedSqlAction[Int, NoStream, Effect.Write] = selectQueryV1.update("4")

    log("updateSingleFieldV1", selectActionV1.statements.toSeq)
    log("updateSingleFieldV2", Seq(selectQueryV1.updateStatement))

    val executeSelectQueryV1: Seq[String] = execute(selectActionV1)
    val executeSelectQueryV2: Seq[Student] = execute(selectActionV2)
    val executeUpdateAction: Int = execute(updateAction)

    executeUpdateAction
  }

  def updateMultipleFields(name: String): Int = {
    val query = StudentTableApi.studentTable.filter(_.name === name).map(student => (student.name, student.`class`))
    val newName = Random.nextInt(1000).toString
    val updateAction = query.update(s"$newName", "6")
    log("updateMultipleFieldsV1", updateAction.statements.toSeq)
    log("updateMultipleFieldsV2", Seq(query.updateStatement))
    val executeUpdateAction: Int = execute(updateAction)
    executeUpdateAction
  }

  def updateMultipleFieldsV2(name: String, updatedStudentForm: StudentForm): Int = {
    val query = StudentTableApi.studentTable.filter(_.name === name).map(student => (student.name, student.`class`, student.classTeacherId).mapTo[StudentForm])
    val updateAction = query.update(updatedStudentForm)
    log("updateMultipleFieldsV2", updateAction.statements.toSeq)
    val executeUpdateAction: Int = execute(updateAction)
    executeUpdateAction
  }

}
