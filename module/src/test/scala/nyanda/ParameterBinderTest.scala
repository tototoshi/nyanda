package nyanda

import munit.FunSuite
import javax.sql.DataSource
import org.h2.jdbcx.JdbcDataSource
import scala.util.Using
import java.sql.PreparedStatement
import java.sql.Connection
import java.time.Instant
import java.util.Date
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.LocalDate
import cats.effect.IO

class ParameterBinderTest extends FunSuite:

  private val dsl = Dsl[IO]
  import dsl.{_, given}

  test("Bind Int") {
    val id = 1
    val sql = sql"select id from person where id = $id"
    assertEquals(sql.statement, "select id from person where id = ?")
    assertEquals(sql.params.size, 1)
  }

  test("Bind Short") {
    val id: Short = 1
    val sql = sql"select id from person where id = $id"
    assertEquals(sql.statement, "select id from person where id = ?")
    assertEquals(sql.params.size, 1)
  }

  test("Bind Long") {
    val id = 1L
    val sql = sql"select id from person where id = $id"
    assertEquals(sql.statement, "select id from person where id = ?")
    assertEquals(sql.params.size, 1)
  }

  test("Bind String") {
    val id = "1"
    val sql = sql"select id from person where id = $id"
    assertEquals(sql.statement, "select id from person where id = ?")
    assertEquals(sql.params.size, 1)
  }

  test("Bind java.sql.Timestamp") {
    val createdAt = new java.sql.Timestamp(0)
    val sql = sql"select id from person where created_at > ${createdAt}"
    assertEquals(sql.statement, "select id from person where created_at > ?")
    assertEquals(sql.params.size, 1)
  }

  test("Bind java.util.Date") {
    val createdAt = new Date(0)
    val sql = sql"select id from person where created_at > ${createdAt}"
    assertEquals(sql.statement, "select id from person where created_at > ?")
    assertEquals(sql.params.size, 1)
  }

  test("Bind java.time.Instant") {
    val createdAt = Instant.ofEpochMilli(0)
    val sql = sql"select id from person where created_at > ${createdAt}"
    assertEquals(sql.statement, "select id from person where created_at > ?")
    assertEquals(sql.params.size, 1)
  }

  test("Bind java.time.LocalDateTime") {
    val createdAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault)
    val sql = sql"select id from person where created_at > ${createdAt}"
    assertEquals(sql.statement, "select id from person where created_at > ?")
    assertEquals(sql.params.size, 1)
  }

  test("Bind java.time.ZonedDateTime") {
    val createdAt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault)
    val sql = sql"select id from person where created_at > ${createdAt}"
    assertEquals(sql.statement, "select id from person where created_at > ?")
    assertEquals(sql.params.size, 1)
  }

  test("Bind Option[T]") {
    val id1: Option[Int] = Some(1)
    val id2: Option[Int] = None
    val sql = sql"select id from person where id = $id1 or id = $id2"
    assertEquals(sql.statement, "select id from person where id = ? or id = ?")
    assertEquals(sql.params.size, 2)
  }

  test("Bind Some[T]") {
    val id: Some[Int] = Some(1)
    val sql = sql"select id from person where id = $id"
    assertEquals(sql.statement, "select id from person where id = ?")
    assertEquals(sql.params.size, 1)
  }

  test("Bind None") {
    val id: None.type = None
    val sql = sql"select id from person where id = $id"
    assertEquals(sql.statement, "select id from person where id = ?")
    assertEquals(sql.params.size, 1)
  }
