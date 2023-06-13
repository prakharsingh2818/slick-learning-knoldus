package com.knoldus.learning.dao

import com.knoldus.learning.model.{Student, StudentV2, Teacher}
import com.knoldus.learning.tables.{StudentTable, StudentTableApi, TeacherTableApi}
import com.knoldus.learning.utils.DbUtils.execute
import com.knoldus.learning.utils.LogUtils.logSqlStatement
import com.typesafe.scalalogging.Logger
import slick.dbio.Effect
import slick.jdbc.GetResult
import slick.jdbc.PostgresProfile.api._
import slick.sql.FixedSqlStreamingAction


object SelectDBFunctions {
  private val baseLogger = Logger(getClass.getName)
  private val log = logSqlStatement(baseLogger)_

  def selectAllStudents(): Seq[Student] = {
    val result = StudentTableApi.studentTable.result
    log("selectAllStudents", result.statements.toSeq)
    val executeSelectAll: Seq[Student] = execute(result)
    executeSelectAll
  }

  def selectAllStudentsNameAndClass(): Seq[StudentV2] = {
    val result = StudentTableApi.studentTable.map(studentDbRecord =>(studentDbRecord.name, studentDbRecord.`class`, studentDbRecord.classTeacherId).mapTo[StudentV2]).result
    log("selectAllStudentsNameAndClass", result.statements.toSeq)
    val executeSelectAll: Seq[StudentV2] = execute(result)
    executeSelectAll
  }


  // .result converts
  def selectSomeStudents(): Seq[Student] = {
    // val result: FixedSqlStreamingAction[Seq[Student], Student, Effect.Read] = StudentTableApi.studentTable.filter(_.`class` <= "12").result
    // can try === and =!=
    val result = StudentTableApi.studentTable.filter(_.`class`.asColumnOf[Int] <= 10).result
    log("selectSomeStudents", result.statements.toSeq)
    val executeSelectSome: Seq[Student] = execute(result)
    executeSelectSome
  }

  def selectSomeStudentsName(): Seq[String] = {
    val result: FixedSqlStreamingAction[Seq[String], String, Effect.Read] = StudentTableApi.studentTable.filter(_.`class`.asColumnOf[Int] <= 10).map(_.name).result
    log("selectSomeStudentsName", result.statements.toSeq)
    val executeSelectSomeName = execute(result)
    executeSelectSomeName
  }

  def selectStudentsByName(name: String): Boolean = {
    val result1: Query[StudentTable, Student, Seq] = for {
      students <- StudentTableApi.studentTable
      if students.name like s"%$name%"
    } yield students

    // val result2 = StudentTableApi.studentTable.filter(_.name like s"%$name%")

    val result3 = result1.exists.result
    log("selectStudentsByNameResult1", result1.result.statements.toSeq)
    // log("selectStudentsByNameResult2", result2.result.statements.toSeq)
    log("selectStudentsByNameResult3", result3.statements.toSeq)

    // val executeSelectStudentsByNameResult1 = execute(result1.result)
    // val executeSelectStudentsByNameResult2 = execute(result2.result)
    val executeSelectStudentsByNameResult3 = execute(result3)

    executeSelectStudentsByNameResult3

  }

  def selectStudentsDesc(): Seq[Student] = {
    val result = StudentTableApi.studentTable.sortBy(student => (student.name.desc, student.`class`.asc)).result
    log("selectStudentsDesc", result.statements.toSeq)
    val executeSelectStudentDesc = execute(result)
    executeSelectStudentDesc
  }

  def selectSomeStudentsV2(name: Option[String] = None): Seq[Student] = {
    val result = StudentTableApi.studentTable.filterOpt(name)((key, value) => key.name === value).result
    log("selectSomeStudentsV2", result.statements.toSeq)
    val executeSelectSomeStudentsV2 = execute(result)
    executeSelectSomeStudentsV2
  }

  def selectSeniorStudents(): Seq[Student] = {
    val result = StudentTableApi.studentTable.filterIf(shouldGetSeniorStudents)(_.`class`.asColumnOf[Int] >= 10).result
    log("selectSeniorStudents", result.statements.toSeq)
    val executeSelectSeniorStudents = execute(result)
    executeSelectSeniorStudents
  }

  private def shouldGetSeniorStudents = true

  def getClassTeacherInformation(): Seq[Teacher] = {
    val joinQuery = StudentTableApi.studentTable
      .filter(_.`class` === "5")
      .join(TeacherTableApi.teacherTable)
      .on(_.classTeacherId === _.id)
      .map(_._2)
      .result

    log("getClassTeacherInformation", joinQuery.statements.toSeq)
    execute(joinQuery)
  }

  def getStudentCountByClass(): Seq[(Int, List[String])] = {
    implicit val getCustomResult: GetResult[(Int, List[String])] = GetResult(result => (result.nextInt, result.nextString().split(",").toList))
    val sqlQuery = sql"""select class, array_agg(name) as names from students group by class""".as[(Int, List[String])]
    execute(sqlQuery)
  }

}
