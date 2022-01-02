package sjdbc.core

import munit.FunSuite
import javax.sql.DataSource
import org.h2.jdbcx.JdbcDataSource
import scala.util.Using
import java.sql.Connection

class AllTest extends FunSuite:

  private val dataSource: DataSource =
    val ds = new JdbcDataSource()
    ds.setUrl("jdbc:h2:mem:core-AllTest;DB_CLOSE_DELAY=1")
    ds.setUser("sa")
    ds.setPassword("")
    ds

  case class Person(id: Int, name: String)

  def ddl(conn: Connection): Int =
    conn.update(sql"""
      create table if not exists person(
        id integer not null,
        name varchar(32) not null,
        primary key(id)
      )
    """)

  def truncate(conn: Connection): Int =
    conn.update(sql"truncate table person")

  override def beforeEach(context: BeforeEach): Unit =
    Using.resource(dataSource.getConnection) { conn =>
      ddl(conn)
    }

  override def afterEach(context: AfterEach): Unit =
    Using.resource(dataSource.getConnection) { conn =>
      truncate(conn)
    }

  test("Query Tests") {
    val person1 = Person(1, "Takahashi")
    val person2 = Person(2, "Suzuki")
    val person3 = Person(3, "Sato")

    val people =
      List(
        person1,
        person2,
        person3
      )

    val parser = ResultSetParser { rs => Person(id = rs.int("id"), name = rs.string("name")) }

    Using.resource(dataSource.getConnection()) { conn =>
      people.foreach { p =>
        conn.update(sql"insert into person (id, name) values (${p.id}, ${p.name})")
      }

      val result1 = conn.query(sql"select id, name from person where id = ${1}").option(parser)
      val result2 = conn.query(sql"select id, name from person order by id").seq(parser)

      assertEquals(result1, Some(Person(1, "Takahashi")))
      assertEquals(result2, people)
    }
  }
