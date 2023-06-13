package com.knoldus.learning.dao

import com.knoldus.learning.model.{Student, Teacher}
import com.knoldus.learning.tables.{StudentTableApi, TeacherTableApi}
import com.knoldus.learning.utils.DbUtils.execute
import com.knoldus.learning.utils.LogUtils.logSqlStatement
import com.typesafe.scalalogging.Logger
import slick.dbio.{Effect, NoStream}
import slick.jdbc.PostgresProfile
import slick.sql.FixedSqlAction
import slick.jdbc.PostgresProfile.api._

import scala.annotation.unused

object InsertDBFunctions {
  private val baseLogger = Logger(getClass.getName)
  private val log = logSqlStatement(baseLogger)_

  // why lazy?
  private lazy val studentReturningStudent: PostgresProfile.ReturningInsertActionComposer[Student, Student] = StudentTableApi.studentTable returning StudentTableApi.studentTable

  private val newStudent = Student(name = "Prakhar", `class` = "12")
  private val anotherNewStudent = Student(id = Some(0), name = "Ayush", `class` = "5")
  private val thirdNewStudent = Student(name = "Gaurav", `class` = "12")
  private val fourthStudent = Student(name = "Rishabh", `class` = "8")

  private val freshTestData: Seq[Student] = Seq(
    Student(name = "Roth", `class` = "6"),
    Student(name = "Vikas", `class` = "10"),
    Student(name = "John", `class` = "4")
  )
  private val newStudentsBatch = Seq(
    Student(name = "Mike", `class` = "11"),
    Student(name = "Jack", `class` = "2"),
  )

  private val teacherOne = Teacher(name = "Ramesh", subject = "Math")
  private val teacherTwo = Teacher(name = "Suresh", subject = "Science")

  /** Student Functions */
  def addNewStudent(): Int = {
    val insertStudent = StudentTableApi.studentTable += newStudent
    log("addNewStudent", insertStudent.statements.toSeq)
    val executeInsertStudent: Int = execute(insertStudent)
    executeInsertStudent
  }

  def forceInsertStudentWithId(): Int = {
    val forceInsertStudentWithId = StudentTableApi.studentTable forceInsert anotherNewStudent
    log("forceInsertStudentWithId", forceInsertStudentWithId.statements.toSeq)
    val executeForceInsertStudentWithId = execute(forceInsertStudentWithId)
    executeForceInsertStudentWithId
  }

  def insertAndReturnId(): Option[Int] = {
    val insertAndReturnId = StudentTableApi.studentTable returning StudentTableApi.studentTable.map(_.id) += thirdNewStudent
    log("insertAndReturnId", insertAndReturnId.statements.toSeq)
    val executeInsertAndReturnId = execute(insertAndReturnId)
    executeInsertAndReturnId
  }

  def insertAndReturn(): Student = {
    val insertAndReturn = studentReturningStudent += fourthStudent
    log("insertAndReturn", insertAndReturn.statements.toSeq)
    val executeInsertAndReturn = execute(insertAndReturn)
    executeInsertAndReturn
  }

  def insertStudents(): Option[Int] = {
    val insertStudents: FixedSqlAction[Option[Int], NoStream, Effect.Write] = StudentTableApi.studentTable ++= freshTestData
    log("insertStudents", insertStudents.statements.toSeq)
    val executeInsertStudents: Option[Int] = execute(insertStudents)
    executeInsertStudents
  }

  def insertStudentsAndReturn(): Seq[Student] = {
    val insertStudentsAndReturn = studentReturningStudent ++= newStudentsBatch
    log("insertStudentsAndReturn", insertStudentsAndReturn.statements.toSeq)
    val executeInsertStudentsAndReturn = execute(insertStudentsAndReturn)
    executeInsertStudentsAndReturn
  }

  /** Teacher Functions */
  def insertAndSelectNewTeacher(): Seq[Teacher] = {
    val insertDbAction = TeacherTableApi.teacherTable += teacherOne
    val selectAction = TeacherTableApi.teacherTable.filter(_.name === teacherOne.name).result
    val combinedAction = insertDbAction andThen selectAction
    val executeCombinedAction = execute(combinedAction)
    executeCombinedAction
  }

  def updateAndInsertTeacherInTransaction(): Unit = {
    val updateAction = TeacherTableApi.teacherTable.filter(_.name === teacherOne.name).map(_.name).update("Ramesh Singh")
    val insertAction = TeacherTableApi.teacherTable += teacherTwo

    val actions = DBIO.seq(updateAction, insertAction)
    execute(actions.transactionally)
  }

  def setClassTeacher(): Int = {
    val updateStudentTable = StudentTableApi.studentTable.filter(_.`class` === "5")
      .map(_.classTeacherId)
      .update(Some(1))
    log("setClassTeacher", updateStudentTable.statements.toSeq)
    val updateAction = execute(updateStudentTable)
    updateAction
  }
}
