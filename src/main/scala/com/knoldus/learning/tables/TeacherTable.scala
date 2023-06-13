package com.knoldus.learning.tables

import com.knoldus.learning.model.Teacher
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape
import slick.sql.SqlProfile.ColumnOption.NotNull

class TeacherTable(tag: Tag) extends Table[Teacher](tag, Some("public"), "teachers"){
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name", NotNull)
  def subject = column[String]("subject", NotNull)

  override def * : ProvenShape[Teacher] = (id, name, subject) <> (Teacher.tupled, Teacher.unapply)
}

object TeacherTableApi {
  lazy val teacherTable: TableQuery[TeacherTable] = TableQuery[TeacherTable]
}
