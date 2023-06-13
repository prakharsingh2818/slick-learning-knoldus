package com.knoldus.learning.dao

import com.knoldus.learning.model.{Student, Teacher}
import com.knoldus.learning.tables.{StudentTableApi, TeacherTableApi}
import com.knoldus.learning.utils.DbUtils
import org.scalatest.{BeforeAndAfter, BeforeAndAfterEach}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import slick.jdbc.PostgresProfile.api._

class InsertDBFunctionsSpec extends AnyFlatSpec with BeforeAndAfterEach with BeforeAndAfter {

  val testData = Seq(
    Student(name = "John", `class` = "9"),
    Student(name = "Hunter", `class` = "11"),
    Student(name = "Mike", `class` = "5")
  )

  override def beforeEach() = {
    DbUtils.execute(StudentTableApi.studentTable.delete)
    DbUtils.execute(TeacherTableApi.teacherTable.delete)
    DbUtils.execute(StudentTableApi.studentTable ++= testData)
  }

  override def afterEach() = {
    DbUtils.execute(StudentTableApi.studentTable.delete)
    DbUtils.execute(TeacherTableApi.teacherTable.delete)
    DbUtils.execute(StudentTableApi.studentTable ++= testData)
  }

  after {
    DbUtils.execute(StudentTableApi.studentTable.delete)
    DbUtils.execute(TeacherTableApi.teacherTable.delete)
  }


  behavior of "InsertDBFunctionsSpec"

  it should "setClassTeacher" in {
    val rowsUpdated = InsertDBFunctions.setClassTeacher()
    assert(rowsUpdated  >= 1)

  }

  it should "forceInsertStudentWithId" in {
    val rowsInserted = InsertDBFunctions.forceInsertStudentWithId()
    assert(rowsInserted >= 1)
  }

  it should "insertAndReturnId" in {
    val id = InsertDBFunctions.insertAndReturnId()
    assert(id.get >= 1)
  }

  it should "insertAndReturn" in {
    val newStudent = InsertDBFunctions.insertAndReturn()
    assert(newStudent.id.get >= 1)
    assert(newStudent.name ++ newStudent.`class` != "")
  }

  it should "insertAndSelectNewTeacher" in {
    val insertAndSelect = InsertDBFunctions.insertAndSelectNewTeacher()
    assert(insertAndSelect.size == 1)
  }

  it should "insertStudents" in {
    val insert = InsertDBFunctions.insertStudents()
    assert(insert.get > 1)
  }

  it should "addNewStudent" in {
    val newStudent = InsertDBFunctions.addNewStudent()
    assert(newStudent == 1)
  }

  it should "insertStudentsAndReturn" in {
    val addStudentsAndReturn = InsertDBFunctions.insertStudentsAndReturn()
    assert(addStudentsAndReturn.size > 1)

    DbUtils.execute(StudentTableApi.studentTable.delete)
    DbUtils.execute(TeacherTableApi.teacherTable.delete)
  }

}
