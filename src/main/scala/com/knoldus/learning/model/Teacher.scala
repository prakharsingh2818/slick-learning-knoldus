package com.knoldus.learning.model

case class Teacher (
  id: Int = -1,
  name: String,
  subject: String
) {
  lazy val form = StudentForm(name, subject)
}

case class TeacherForm (
  name: String,
  subject: String
)
