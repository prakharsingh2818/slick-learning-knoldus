package com.knoldus.learning.model

case class Student (
  id: Option[Int] = None,
  name: String,
  `class`: String,
  classTeacherId: Option[Int] = None
) extends Product with Serializable {
  lazy val form = StudentForm(name, `class`, classTeacherId)
}

case class StudentV2 (
  name: String,
  `class`: String,
  classTeacherId: Option[Int] = None
) extends Product with Serializable

case class StudentForm (
  name: String,
  `class`: String,
  classTeacherId: Option[Int] = None
) extends Product with Serializable