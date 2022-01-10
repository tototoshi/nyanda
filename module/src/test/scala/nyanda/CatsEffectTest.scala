package nyanda

import munit.FunSuite
import javax.sql.DataSource
import cats._
import cats.data.Kleisli
import cats.syntax._
import cats.implicits._
import cats.effect._
import cats.effect.implicits._
import cats.effect.unsafe.implicits.global
import org.h2.jdbcx.JdbcDataSource

class CatsEffectTest extends FunSuite with Dsl[IO]:

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

  given ResultSetRead[IO, Person] = ResultSetRead {
    import RS._
    val id = get[Int]("id")
    val name = get[String]("name")
    val nickname = get[Option[String]]("nickname")
    (id, name, nickname).mapN(Person.apply)
  }

  def ddl = DB.update(sql"""
      create table person(
        id integer not null,
        name varchar(32) not null,
        nickname varchar(32),
        primary key(id)
      )
      """)

  def drop: Query[Int] = DB.update(sql"drop table person")

  def insertAll(people: List[Person]): Query[List[Int]] = people.traverse(insert)

  def insert(p: Person): Query[Int] =
    DB.update(sql"insert into person (id, name, nickname) values (${p.id}, ${p.name}, ${p.nickname})")

  def findById(id: Int): Query[Option[Person]] = DB.query(sql"select id, name, nickname from person where id = ${id}")

  def findAll: Query[Seq[Person]] = DB.query(sql"select id, name, nickname from person")

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
          _ <- insertAll(people)
          result1 <- findById(1)
          result2 <- findAll
        } yield (result1, result2)).run
      }

    assertEquals(program.unsafeRunSync(), (Some(person1), people))
  }

  test("autoCommit") {
    val program1: IO[Boolean] =
      dataSource
        .autoCommit[IO]
        .use { conn => insertAll(people)(conn) *> IO.raiseError(new RuntimeException("error")) }
        .handleError(e => true)

    assertEquals(program1.unsafeRunSync(), true)

    val program2 = dataSource
      .readOnly[IO]
      .use(findAll.run)

    assertEquals(program2.unsafeRunSync(), people)
  }

  test("transaction (Success)") {
    val program1: IO[List[Int]] =
      dataSource
        .transaction[IO]
        .use(insertAll(people).run)

    program1.unsafeRunSync()

    val program2 =
      dataSource
        .readOnly[IO]
        .use(findAll.run)

    assertEquals(program2.unsafeRunSync(), people)
  }

  test("transaction (Failure)") {
    val program1: IO[Boolean] =
      dataSource
        .transaction[IO]
        .use { conn => insertAll(people).run(conn) *> IO.raiseError(new RuntimeException("error")) }
        .handleError(e => true)

    assertEquals(program1.unsafeRunSync(), true)

    val program2 =
      dataSource
        .readOnly[IO]
        .use(findById(1).run)

    assertEquals(program2.unsafeRunSync(), None)
  }
