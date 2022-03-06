package nyanda

import munit.FunSuite
import cats.*
import cats.data.Kleisli
import cats.syntax.*
import cats.implicits.*
import cats.effect.*
import cats.effect.implicits.*
import cats.effect.unsafe.implicits.global
import org.h2.jdbcx.JdbcDataSource

class ResultSetConsumeTest extends FunSuite with Dsl.IO:

  val ds = new JdbcDataSource()
  ds.setUrl("jdbc:h2:mem:ResultSetConsumeTest")
  ds.setUser("sa")
  ds.setPassword("")

  val t = Transactor[IO](ds)

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
    import RS.*
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

  def drop: Query[IO, Int] = DB.update(sql"drop table person")

  def insertAll(people: List[Person]): Query[IO, List[Int]] = people.traverse(insert)

  def insert(p: Person): Query[IO, Int] =
    DB.update(sql"insert into person (id, name, nickname) values (${p.id}, ${p.name}, ${p.nickname})")

  def findById(id: Int): Query[IO, Option[Person]] =
    DB.query(sql"select id, name, nickname from person where id = ${id}")

  def findAll: Query[IO, Seq[Person]] = DB.query(sql"select id, name, nickname from person")

  test("for T") {
    val program =
      t.transaction.use {
        (for
          _ <- ddl *> insertAll(people)
          result1 <- DB.query[Person](sql"select id, name, nickname from person where id = 1")
          result2 <- DB.query[Person](sql"select id, name, nickname from person where id = 2")
          result3 <- DB.query[Person](sql"select id, name, nickname from person where id = 3")
          result4 <- DB
            .query[Person](sql"select id, name, nickname from person where id = 4")
            .handleErrorWith(_ => Kleisli.liftF(IO.pure(null.asInstanceOf[Person])))
          _ <- Kleisli.liftF(IO {
            assertEquals(result1, person1)
            assertEquals(result2, person2)
            assertEquals(result3, person3)
            assertEquals(result4, null.asInstanceOf[Person])
          })
        yield result1).run
      }

    program.unsafeRunSync()
  }

  test("for Option[T]") {
    val program =
      t.transaction.use {
        (for
          _ <- ddl *> insertAll(people)
          result1 <- DB.query[Option[Person]](sql"select id, name, nickname from person where id = 1")
          result2 <- DB.query[Option[Person]](sql"select id, name, nickname from person where id = 2")
          result3 <- DB.query[Option[Person]](sql"select id, name, nickname from person where id = 3")
          result4 <- DB
            .query[Option[Person]](sql"select id, name, nickname from person where id = 4")
          _ <- Kleisli.liftF(IO {
            assertEquals(result1, Some(person1))
            assertEquals(result2, Some(person2))
            assertEquals(result3, Some(person3))
            assertEquals(result4, None)
          })
        yield result1).run
      }

    program.unsafeRunSync()
  }

  test("for Seq[T]") {
    val program =
      t.transaction.use {
        (for
          _ <- ddl *> insertAll(people)
          result1 <- DB.query[Seq[Person]](sql"select id, name, nickname from person where id = 1")
          result2 <- DB.query[Seq[Person]](sql"select id, name, nickname from person")
          result3 <- DB.query[Seq[Person]](sql"select id, name, nickname from person where id > 3")
          _ <- Kleisli.liftF(IO {
            assertEquals(result1, Seq(person1))
            assertEquals(result2, Seq(person1, person2, person3))
            assertEquals(result3, Seq.empty[Person])
          })
        yield result1).run
      }

    program.unsafeRunSync()
  }

  test("for List[T]") {
    val program =
      t.transaction.use {
        (for
          _ <- ddl *> insertAll(people)
          result1 <- DB.query[Seq[Person]](sql"select id, name, nickname from person where id = 1")
          result2 <- DB.query[Seq[Person]](sql"select id, name, nickname from person")
          result3 <- DB.query[Seq[Person]](sql"select id, name, nickname from person where id > 3")
          _ <- Kleisli.liftF(IO {
            assertEquals(result1, Seq(person1))
            assertEquals(result2, Seq(person1, person2, person3))
            assertEquals(result3, Seq.empty[Person])
          })
        yield result1).run
      }

    program.unsafeRunSync()
  }
