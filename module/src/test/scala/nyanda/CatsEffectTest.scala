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
import nyanda.syntax._
import org.h2.jdbcx.JdbcDataSource

class CatsEffectTest extends FunSuite:

  val dataSource =
    val ds = new JdbcDataSource()
    ds.setUrl("jdbc:h2:mem:effect;DB_CLOSE_DELAY=1")
    ds.setUser("sa")
    ds.setPassword("")
    ds

  case class Person(id: Int, name: String, nickname: Option[String])

  val person1 = Person(1, "Takahashi", Some("Taka"))
  val person2 = Person(2, "Suzuki", None)
  val person3 = Person(3, "Sato", None)

  val people =
    List(
      person1,
      person2,
      person3
    )

  private val db: DB[IO] = DB[IO]
  import db._

  implicit def reader2: ResultSetRead[IO, Person] = ResultSetRead {
    val id = get[Int]("id")
    val name = get[String]("name")
    val nickname = get[Option[String]]("nickname")
    (id, name, nickname).mapN(Person.apply)
  }

  def ddl: Kleisli[IO, Connection[IO], Int] = update(sql"""
      create table person(
        id integer not null,
        name varchar(32) not null,
        nickname varchar(32),
        primary key(id)
      )
      """)

  def drop: Kleisli[IO, Connection[IO], Int] = update(sql"drop table person")

  def insert(people: List[Person]): Kleisli[IO, Connection[IO], List[Int]] =
    people.traverse { p =>
      update(sql"insert into person (id, name, nickname) values (${p.id}, ${p.name}, ${p.nickname})")
    }

  override def beforeEach(context: BeforeEach): Unit =
    dataSource
      .autoCommit[IO]
      .use(ddl.run)
      .unsafeRunSync()

  override def afterEach(context: AfterEach): Unit =
    dataSource
      .autoCommit[IO]
      .use(drop.run)
      .unsafeRunSync()

  test("Query Tests") {

    val program: IO[(Option[Person], Seq[Person])] =
      dataSource.transaction[IO].use {
        (for {
          _ <- people.traverse { p =>
            update(sql"insert into person (id, name, nickname) values (${p.id}, ${p.name}, ${p.nickname})")
          }
          result1 <- query(sql"select id, name, nickname from person where id = ${1}") >>> as[Option[Person]]
          result2 <- query(sql"select id, name, nickname from person") >>> as[Seq[Person]]
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
      .use((query(sql"select id, name, nickname from person") >>> as[Seq[Person]]).run)

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
        .use((query(sql"select id, name, nickname from person") >>> as[Seq[Person]]).run)

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
        .use((query(sql"select id, name, nickname from person where id = ${1}") >>> as[Option[Person]]).run)

    assertEquals(program2.unsafeRunSync(), None)
  }
