package com.knoldus.learning.tables

import com.knoldus.learning.model.Student
import slick.lifted.{ProvenShape, Tag}
import slick.jdbc.PostgresProfile.api._
import slick.sql.SqlProfile.ColumnOption.NotNull

// tag - slick.table.lifted
// table - slick.model
class StudentTable(tag: Tag) extends Table[Student](tag, Some("public"), "students") {
  def id = column[Option[Int]]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name", NotNull)
  def `class` = column[String]("class", NotNull)
  def classTeacherId = column[Option[Int]]("class_teacher_id")

  def studentsNameClassIdx = index("students_name_class_idx", (name, `class`))

  override def * : ProvenShape[Student] = (id, name, `class`, classTeacherId).mapTo[Student]
}

object StudentTableApi {
  /**
   * TableQuery is a subtype of Query that SLick uses to represent select, update and delete queries
   */
  lazy val studentTable: TableQuery[StudentTable] = TableQuery[StudentTable]

  val createStudentTableDdl: Iterator[String] = studentTable.schema.createStatements
}
