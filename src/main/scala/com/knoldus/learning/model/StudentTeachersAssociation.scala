package com.knoldus.learning.model

case class StudentTeachersAssociation (
  id: String,
  studentId: String,
  teacherId: List[String]
)
