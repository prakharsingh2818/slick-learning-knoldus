package com.knoldus.learning

import com.knoldus.learning.dao.{DeleteDBFunctions, InsertDBFunctions, SelectDBFunctions, UpdateDBFunctions}
import com.knoldus.learning.healthcheck.DBHealthCheck
import com.knoldus.learning.model.StudentForm

/**
 * Query is used to build SQL for a single query. Calls to map and filter modify clauses to the SQL, but only ne query is created.
 * DBIOAction is used to build sequences of SQL queries. Calls to map and filter chain queries together and transform their results once they are retrieved in the database. DBIOAction is also used to delineate transactions.
 * Future is used to transform the asynchronous result of running a DBIOAction. Transformations of Futures happen after we have finished speaking to the database.
 *
 * We define the mappings between Scala case classes and tuples and the Database using Table classes.
 * We define queries by creating  TableQuery objects and transforming them with combinators such as map and filter. The transformations look like transformations on collections, but they are used to build SQL code rather than manipulate the results returned.
 * We execute a query by creating an action object via its result method. Actions are used to build sequences of related queries and wrap them in transactions
 * Finally, we run the action against the database by passing it to the run method of the database object and the result is given back as a Future
 *
 * In many cases (eg: select queries) we create a Query first and convert it to a DBIOAction using the result method.
 * In other cases (eg: insert queries), the Slick API gives us a DBIOAction immediately, bypassing Query.
 * In all cases, we runa  DBIOAction using db.run(...), turning it into a Future of the result
 *
 *
 * ******************************************************
 *
 * Slick represents all queries using a trait Query[M, U, C] that has 3 type parameters
 * M: mixed type. This is the function parameter type we see when calling methods like map and filter
 * U: unpacked type. This is the type we collect in our results.
 * C: collection type. This is the type of collection we accumulate results into
 *
 * A TableQuery is actually a Query that uses a Table (e.g. StudentTable) as its mixed type
 * and the table's element type (the type parameter in the constructor, e.g. Student as its unpacked type.
 * In other words the function we provide to studentTable.filter is actually passed a parameter of type StudentTable
 *
 *
 * **********************************************************
 *
 * During filter operation we know the type of column we are comparing so it is type safe in that way
 *
 *
 *
 * ********************************************************
 *
 * For DB Health-checks, we can have Query(1)
 * Query(1).result.statements.mkString will be "select 1"
 * The apply method of Query allows us to lift scalar value to a Query
 *
 *
 * *********************************************************
 *
 * The map method
 * - We use this when we don't want to select all columns. This changes both the mixed and unpacked type of Query
 * - Actual query to the DB is modified here as well
 * - See selectSomeStudentsName method. The 2nd parameter (unpacked) changes to the type of column(s)
 * - The 1st parameter also changes accordingly. SO for eg: for name column, 2nd parameter will become String
 * and 1st parameter becomes Rep[String] which will be passed when we map or filter over it.
 *
 * - If we map over the TableQuery to select 2 columns of type, say, Long and String the type of result will be:
 * Query[(Rep[Long], Rep[String]), (Long, String), Seq]
 *
 * - After map we can use mapTo to map result onto Scala classes
 * Eg: selectAllStudentsNameAndClass
 *
 *
 * *******************************************************
 *
 * The flatMap method
 * - Used for joins
 *
 *
 * ********************************************************
 *
 * exists method
 *
 *
 * ***********************************************************
 *
 * QUERIES ===> ACTIONS
 * Before running a query we need to convert it to an "action".
 * This is done typically by calling the result method on the query.
 * Actions have the type signature DBIOAction[R, S, E].
 *  - R: Type of data we expect to get back from the DB (Eg: Student)
 *  - S: Indicates whether the results are streamed (Streaming[T]) or not (NoStream)
 *  - E: Effect type and will be inferred.
 *
 * In many cases, we can simplify the representation of an action to just DBIO[T], which is an alias
 * for DBIOAction[T, NoStream, Effect.All]
 *
 *  - Effect is a way to annotate an action. Eg:
 *    - Read for queries that read from the database
 *    - Write for queries that write effect on the database
 *    - Scheme for schema effects
 *    - Transactional for transaction effects
 *    - All for all of the above
 *
 *
 *
 * *******************************************
 *
 * EXECUTING ACTIONS
 * 2 ways:
 *  - db.run(...) runs the actions and returns all the results in a single collection.
 *    These are known as a materialized result. It returns Future of final result of our "action".
 *  - db.stream(...) runs the action and returns its results in a Stream, allowing us to process large
 *    datasets incrementally without consuming large amounts of memory
 *
 * Calling db.stream returns a DatabasePublisher object instead of a Future. With this we have access to 3 methods:
 *    - subscribe: which allows integration with Akka;
 *    - mapResult: which creates a new Publisher that maps the supplied function on the result set
 *      from the original publisher; and
 *    - foreach, to perform a side-effect with the result
 *
 *
 * *********************************************************
 *
 * Can model "null" as Option
 *
 *
 * *********************************************************
 *
 * "map" acts like a SQL select and filter is like a WHERE.
 * unpacked type: regular Scala types we work with like String,
 * mixed type: is Slick's column representation, such as Rep[String]
 *
 * We run "queries" by converting them to "actions" using the result method. We run the actions against a database using db.run
 *
 *
 * =================================== CHAPTER3 ===================================
 *
 *
 *
 * Two ways of inserting rows:
 * - += : Inserts a single row
 * - ++- : Inserts multiple rows
 *
 *
 * Using += directly creates a DBIOAction without any intermediate Query. Result of the action is the
 * number of rows inserted.
 *
 * When inserting data, we need to tell the database whether or not to allocate primary keys for the new rows.
 * Slick allows us to allocate auto-incrementing primary keys via an option on the column definition.
 * Eg: O.AutoInc
 * This would imply that Slick can omit the column in the corresponding SQL
 *
 * To force insert ID we use the "forceInsert" function
 *
 * To retrieve primary key on insert we use the "returning" function.
 * The argument passed to the "returning" is a Query over the same table
 *
 * Batch insert is possible via ++=. The only difference is the argument type (which is a collection in this case)
 * and the return type which is an Option[Int]. This is because JDBC specification permits the underlying database driver
 * to indicate the number of rows inserted is unknown
 *
 *
 * ***********************   DELETE
 *
 * We specify the rows to delete using the filter method and then call delete
 *
 * The return type is the number of rows deleted. "delete" cannot be in combination with map.
 * We can only call delete on a TableQuery
 *
 * ************************ UPDATE
 *
 * To update, we start by creating a query to select the rows to modify and the columns to change.
 * Then we can use "update" to turn this into an action to run
 *
 *
 *
 *  =================================== CHAPTER4 ===================================
 *
 *  In Slick, we use action combinators like map, fold and zip to turn a number of actions into a single action.
 *  Combined actions are not
 *
 *  1. andThen (or >>): The simplest way to run an action after another. Both actions run but result of
 *  the second is returned.
 *
 *
 */
object Driver extends App {

  DBHealthCheck.ping()

  // println(StudentTableApi.createStudentTableDdl.mkString)
  /** Insert operation */

  // println(InsertDBFunctions.insertStudents())

  /** Select operation */
  // println(SelectDBFunctions.selectAllStudents())

  /** Select with Map */
  // println(SelectDBFunctions.selectAllStudentsNameAndClass())

  /** Select wih Filter */
  // println(SelectDBFunctions.selectSomeStudents())

  /** Select Specific Column wih Filter */
  // println(SelectDBFunctions.selectSomeStudentsName())

  // println(SelectDBFunctions.selectStudentsByName(name = "Roth"))

  // println(SelectDBFunctions.selectStudentsDesc())

  /** filterOpt */
  println(SelectDBFunctions.selectSomeStudentsV2()) // would return all students
  //println(SelectDBFunctions.selectSomeStudentsV2(name = Some("Roth"))) // would filter on name

  /**
   * Combining Queries
   * Query is a monad
   */
  /*
  import slick.jdbc.PostgresProfile.api._
  val output = for {
    result <- StudentTableApi.studentTable
    if result.`class` === "12"
  } yield result
*/


  /**
   * Query is used to build SQL for a single query. Calls to map and filter modify clauses to the SQL, but only ne query is created.
   * DBIOAction is used to build sequences of SQL queries. Calls to map and filter chain queries together and transform their results once they are retrieved in the database. DBIOAction is also used to delineate transactions.
   * Future is used to transform the asynchronous result of running a DBIOAction. Transformations of Futures happen after we have finished speaking to the database.
   *
   * We define the mappings between Scala case classes and tuples and the Database using Table classes.
   * We define queries by creating  TableQuery objects and transforming them with combinators such as map and filter. The transformations look like transformations on collections, but they are used to build SQL code rather than manipulate the results returned.
   * We execute a query by creating an action object via its result method. Actions are used to build sequences of related queries and wrap them in transactions
   * Finally, we run the action against the database by passing it to the run method of the database object and the result is given back as a Future
   *
   * In many cases (eg: select queries) we create a Query first and convert it to a DBIOAction using the result method.
   * In other cases (eg: insert queries), the Slick API gives us a DBIOAction immediately, bypassing Query.
   * In all cases, we runa  DBIOAction using db.run(...), turning it into a Future of the result
   */

  /***
   * Slick represents all queries using a trait Query[M, U, C] that has 3 type parameters
   * M: mixed type. This is the function parameter type we see when calling methods like map and filter
   * U: unpacked type. This is the type we collect in our results.
   * C: collection type. This is the type of collection we accumulate results into
   *
   * A TableQuery is actually a Query that uses a Table (e.g. StudentTable) as its mixed type
   * and the table's element type (the type parameter in the constructor, e.g. Student as its unpacked type.
   * In other words the function we provide to studentTable.filter is actually passed a parameter of type StudentTable
   */

  /***
   * During filter operation we know the type of column we are comparing so it is type safe in that way
   */

  /**
   * For DB Health-checks, we can have Query(1)
   * Query(1).result.statements.mkString will be "select 1"
   * The apply method of Query allows us to lift scalar value to a Query
   */

  /***
   * The map method
   * - We use this when we don't want to select all columns. This changes both the mixed and unpacked type of Query
   * - Actual query to the DB is modified here as well
   * - See selectSomeStudentsName method. The 2nd parameter (unpacked) changes to the type of column(s)
   * - The 1st parameter also changes accordingly. SO for eg: for name column, 2nd parameter will become String
   * and 1st parameter becomes Rep[String] which will be passed when we map or filter over it.
   *
   * - If we map over the TableQuery to select 2 columns of type, say, Long and String the type of result will be:
   * Query[(Rep[Long], Rep[String]), (Long, String), Seq]
   *
   * - After map we can use mapTo to map result onto Scala classes
   * Eg: selectAllStudentsNameAndClass
   */

  /**
   * The flatMap method
   * - Used for joins
   */

  /**
   * exists method
   */

  /**
   * QUERIES ===> ACTIONS
   * Before running a query we need to convert it to an "action".
   * This is done typically by calling the result method on the query.
   * Actions have the type signature DBIOAction[R, S, E].
   *  - R: Type of data we expect to get back from the DB (Eg: Student)
   *  - S: Indicates whether the results are streamed (Streaming[T]) or not (NoStream)
   *  - E: Effect type and will be inferred.
   *
   *  In many cases, we can simplify the representation of an action to just DBIO[T], which is an alias
   *  for DBIOAction[T, NoStream, Effect.All]
   *
   *  - Effect is a way to annotate an action. Eg:
   *    - Read for queries that read from the database
   *    - Write for queries that write effect on the database
   *    - Scheme for schema effects
   *    - Transactional for transaction effects
   *    - All for all of the above
   */

  /**
   * EXECUTING ACTIONS
   * 2 ways:
   *  - db.run(...) runs the actions and returns all the results in a single collection.
   *  These are known as a materialized result. It returns Future of final result of our "action".
   *  - db.stream(...) runs the action and returns its results in a Stream, allowing us to process large
   *  datasets incrementally without consuming large amounts of memory
   *
   *  Calling db.stream returns a DatabasePublisher object instead of a Future. With this we have access to 3 methods:
   *    - subscribe: which allows integration with Akka;
   *    - mapResult: which creates a new Publisher that maps the supplied function on the result set
   *    from the original publisher; and
   *    - foreach, to perform a side-effect with the result
   */

  /**
   * Can model "null" as Option
   */

  /**
   * "map" acts like a SQL select and filter is like a WHERE.
   * unpacked type: regular Scala types we work with like String,
   * mixed type: is Slick's column representation, such as Rep[String]
   *
   * We run "queries" by converting them to "actions" using the result method. We run the actions against a database using db.run
   */


  /**
   * Two ways of inserting rows:
   * - += : Inserts a single row
   * - ++- : Inserts multiple rows
   *
   *
   * Using += directly creates a DBIOAction without any intermediate Query. Result of the action is the
   * number of rows inserted.
   *
   * When inserting data, we need to tell the database whether or not to allocate primary keys for the new rows.
   * Slick allows us to allocate auto-incrementing primary keys via an option on the column definition.
   * Eg: O.AutoInc
   * This would imply that Slick can omit the column in the corresponding SQL
   *
   * To force insert ID we use the "forceInsert" function
   *
   * To retrieve primary key on insert we use the "returning" function.
   * The argument passed to the "returning" is a Query over the same table
   *
   * Batch insert is possible via ++=. The only difference is the argument type (which is a collection in this case)
   * and the return type which is an Option[Int]. This is because JDBC specification permits the underlying database driver
   * to indicate the number of rows inserted is unknown
   *
   *
   * ***********************   DELETE
   * We specify the rows to delete using the filter method and then call delete
   *
   * The return type is the number of rows deleted. "delete" cannot be in combination with map.
   * We can only call delete on a TableQuery
   *
   * ************************ UPDATE
   * To update, we start by creating a query to select the rows to modify and the columns to change.
   * Then we can use "update" to turn this into an action to run
   *
   */
  // println(InsertDBFunctions.addNewStudent())

  /** Force Insert with ID */
  // println(InsertDBFunctions.forceInsertStudentWithId())

  /** Retrieving primary keys on insert */
  // println(InsertDBFunctions.insertAndReturnId())

  /** Retrieving complete object on insert */
  // println(InsertDBFunctions.insertAndReturn())

  /** Batch insert */
  // println(InsertDBFunctions.insertStudents())

  /** Batch insert with Returning */
  // println(InsertDBFunctions.insertStudentsAndReturn())

  /** Delete By Name */
  //println(DeleteDBFunctions.deleteByName("Prakhar"))

  /** Update single fields */
  // When student does not exists --> returns 0, no error
  // println(UpdateDBFunctions.updateSingleField("Himanshu"))
  // println(UpdateDBFunctions.updateSingleField("Jack"))

  /** Update multiple fields */
  // println(UpdateDBFunctions.updateMultipleFields("Vikas"))

  /** Update using mapTo */
  // println(UpdateDBFunctions.updateMultipleFieldsV2("Mike", StudentForm("Mike C.", "12")))

  /** Combined Action using andThen */
  // println(InsertDBFunctions.insertAndSelectNewTeacher())

  /** Combined Action using a transaction */
  // println(InsertDBFunctions.updateAndInsertTeacherInTransaction())

  /** Set class teacher */
  // println(InsertDBFunctions.setClassTeacher())

  /** Join */
  // println(SelectDBFunctions.getClassTeacherInformation())

  /** Plain SQL Query */
  // println(SelectDBFunctions.getStudentCountByClass().mkString("\n"))
}