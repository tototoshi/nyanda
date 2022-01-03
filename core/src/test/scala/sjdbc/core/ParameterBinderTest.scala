package sjdbc.core

import munit.FunSuite
import javax.sql.DataSource
import org.h2.jdbcx.JdbcDataSource
import scala.util.Using
import java.sql.PreparedStatement
import java.sql.Connection
import org.mockito.Mockito

class ParameterBinderTest extends FunSuite:

  test("Bind Int") {
    val id = 1
    val sql = sql"select id from person where id = $id"
    assertEquals(sql.statement, "select id from person where id = ?")
    assertEquals(sql.params.size, 1)

    val mock: PreparedStatement = Mockito.mock(classOf[PreparedStatement])
    sql.params.head.bind(mock, 1)
    Mockito.verify(mock).setInt(1, 1)
  }

  test("Bind String") {
    val id = "1"
    val sql = sql"select id from person where id = $id"
    assertEquals(sql.statement, "select id from person where id = ?")
    assertEquals(sql.params.size, 1)

    val mock: PreparedStatement = Mockito.mock(classOf[PreparedStatement])
    sql.params.head.bind(mock, 1)
    Mockito.verify(mock).setString(1, "1")
  }

  test("Bind Option[T]") {
    val id1: Option[Int] = Some(1)
    val id2: Option[Int] = None
    val sql = sql"select id from person where id = $id1 or id = $id2"
    assertEquals(sql.statement, "select id from person where id = ? or id = ?")
    assertEquals(sql.params.size, 2)

    val mock1: PreparedStatement = Mockito.mock(classOf[PreparedStatement])
    sql.params.head.bind(mock1, 1)
    Mockito.verify(mock1).setInt(1, 1)

    val mock2: PreparedStatement = Mockito.mock(classOf[PreparedStatement])
    sql.params(1).bind(mock2, 1)
    Mockito.verify(mock2).setInt(1, null.asInstanceOf[Int])
  }

  test("Bind Some[T]") {
    val id: Some[Int] = Some(1)
    val sql = sql"select id from person where id = $id"
    assertEquals(sql.statement, "select id from person where id = ?")
    assertEquals(sql.params.size, 1)

    val mock: PreparedStatement = Mockito.mock(classOf[PreparedStatement])
    sql.params.head.bind(mock, 1)
    Mockito.verify(mock).setInt(1, 1)
  }

  test("Bind None") {
    val id: None.type = None
    val sql = sql"select id from person where id = $id"
    assertEquals(sql.statement, "select id from person where id = ?")
    assertEquals(sql.params.size, 1)

    val mock: PreparedStatement = Mockito.mock(classOf[PreparedStatement])
    sql.params.head.bind(mock, 1)
    Mockito.verify(mock).setObject(1, null)
  }
