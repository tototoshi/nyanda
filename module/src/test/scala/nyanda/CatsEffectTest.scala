package nyanda

import munit.FunSuite
import javax.sql.DataSource
import java.sql.ResultSet
import cats._
import cats.data.Kleisli
import cats.syntax._
import cats.implicits._
import cats.effect._
import cats.effect._
import cats.effect.implicits._
import cats.effect.unsafe.implicits.global
import java.sql.Connection
import nyanda.syntax._
import org.h2.jdbcx.JdbcDataSource

class CatsEffectTest extends FunSuite:

  val dataSource =
    val ds = new JdbcDataSource()
    ds.setUrl("jdbc:h2:mem:effect;DB_CLOSE_DELAY=1")
    ds.setUser("sa")
    ds.setPassword("")
    ds

  case class Person(id: Int, name: String)

  val person1 = Person(1, "Takahashi")
  val person2 = Person(2, "Suzuki")
  val person3 = Person(3, "Sato")

  val people =
    List(
      person1,
      person2,
      person3
    )

  private val db: DB[IO] = DB[IO]
  import db._

  implicit def reader: ResultSetRead[IO, Person] = ResultSetRead {
    for {
      id <- get[Int]("id")
      name <- get[String]("name")
    } yield Person(id, name)
  }

  def ddl(conn: Connection): IO[Int] =
    update(sql"""
      create table if not exists person(
        id integer not null,
        name varchar(32) not null,
        primary key(id)
      )
    """)(conn)

  def truncate(conn: Connection): IO[Int] = update(sql"truncate table person")(conn)

  def insert(people: List[Person]): Kleisli[IO, Connection, List[Int]] =
    people.traverse { p =>
      update(sql"insert into person (id, name) values (${p.id}, ${p.name})")
    }

  override def beforeEach(context: BeforeEach): Unit =
    dataSource
      .autoCommit[IO]
      .use(ddl)
      .unsafeRunSync()

  override def afterEach(context: AfterEach): Unit =
    dataSource
      .autoCommit[IO]
      .use(truncate)
      .unsafeRunSync()

  test("Query Tests") {

    val program: IO[(Option[Person], Seq[Person])] =
      dataSource.transaction[IO].use {
        (for {
          _ <- people.traverse { p =>
            update(sql"insert into person (id, name) values (${p.id}, ${p.name})")
          }
          result1 <- query(sql"select id, name from person where id = ${1}") >>> as[Option[Person]]
          result2 <- query(sql"select id, name from person") >>> as[Seq[Person]]
        } yield (result1, result2)).run
      }

    assertEquals(program.unsafeRunSync(), (Some(person1), people))
  }

  test("autoCommit") {
    val program1: IO[Boolean] =
      dataSource
        .autoCommit[IO]
        .use { conn => insert(people)(conn) *> IO.raiseError(new RuntimeException("error")) }
        .handleError(e => true)

    assertEquals(program1.unsafeRunSync(), true)

    val program2 = dataSource
      .readOnly[IO]
      .use((query(sql"select id, name from person") >>> as[Seq[Person]]).run)

    assertEquals(program2.unsafeRunSync(), people)
  }

  test("transaction (Success)") {
    val program1: IO[List[Int]] =
      dataSource
        .transaction[IO]
        .use(insert(people).run)

    program1.unsafeRunSync()

    val program2 =
      dataSource
        .readOnly[IO]
        .use((query(sql"select id, name from person") >>> as[Seq[Person]]).run)

    assertEquals(program2.unsafeRunSync(), people)
  }

  test("transaction (Failure)") {
    val program1: IO[Boolean] =
      dataSource
        .transaction[IO]
        .use { conn => insert(people).run(conn) *> IO.raiseError(new RuntimeException("error")) }
        .handleError(e => true)

    assertEquals(program1.unsafeRunSync(), true)

    val program2 =
      dataSource
        .readOnly[IO]
        .use((query(sql"select id, name from person where id = ${1}") >>> as[Option[Person]]).run)

    assertEquals(program2.unsafeRunSync(), None)
  }
