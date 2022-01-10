package nyanda.example

import nyanda._
import cats._
import cats.data.Kleisli
import cats.implicits._
import cats.effect._
import cats.effect.kernel.Resource
import cats.effect.std.Console
import org.h2.jdbcx.JdbcDataSource
import java.time.{ZonedDateTime, ZoneId}

case class Person(id: Int, name: String, nickname: Option[String], createdAt: ZonedDateTime)

trait PersonDao[F[_]: Applicative] extends Dsl[F]:

  private val personGet =
    (RS.get[Int]("id"), RS.get[String]("name"), RS.get[Option[String]]("nickname"), RS.get[ZonedDateTime]("created_at"))
      .mapN(Person.apply)

  given ResultSetRead[F, Person] = ResultSetRead(personGet)

  private val ddl: SQL[F] =
    sql"""
      create table if not exists person(
        id integer not null,
        name varchar(32) not null,
        nickname varchar(32),
        created_at datetime not null,
        primary key(id)
      )
      """

  def createTable: Query[F, Int] = DB.update(ddl)

  def insert(p: Person): Query[F, Int] =
    DB.update(
      sql"insert into person (id, name, nickname, created_at) values (${p.id}, ${p.name}, ${p.nickname}, ${p.createdAt})"
    )

  def findById(id: Int): Query[F, Option[Person]] =
    DB.query(sql"select id, name, nickname, created_at from person where id = ${1}")

  def findAll: Query[F, Seq[Person]] = DB.query(sql"select id, name, nickname, created_at from person")

object Main extends IOApp:

  val dataSource =
    val ds = new JdbcDataSource()
    ds.setUrl("jdbc:h2:mem:example")
    ds.setUser("sa")
    ds.setPassword("")
    ds

  val transactor = Transactor[IO](dataSource)

  val people =
    List(
      Person(1, "Takahashi", "Taka".some, ZonedDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault)),
      Person(2, "Suzuki", None, ZonedDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault)),
      Person(3, "Sato", None, ZonedDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault))
    )

  def personDao[F[_]: Sync] = new PersonDao[F] {}

  def queryGroup[F[_]: Sync: Console]: Query[F, (Option[Person], Seq[Person])] =
    for {
      _ <- personDao.createTable
      _ <- people.traverse(personDao.insert)
      result1 <- personDao.findById(1)
      _ <- Kleisli.liftF(Console[F].println(result1))
      result2 <- personDao.findAll
      _ <- Kleisli.liftF(Console[F].println(result2))
    } yield (result1, result2)

  override def run(args: List[String]): IO[ExitCode] =
    transactor.transaction.use(queryGroup[IO].run).map(_ => ExitCode.Success)

// sbt:root> example/run
// [info] compiling 1 Scala source to /Users/cw-toshiyuki.takahashi/work/github.com/tototoshi/nyanda/example/target/scala-3.1.0/classes ...
// [info] running (fork) nyanda.example.Main
// [info] Some(Person(1,Takahashi,Some(Taka),2021-01-01T00:00+09:00[Asia/Tokyo]))
// [info] List(Person(1,Takahashi,Some(Taka),2021-01-01T00:00+09:00[Asia/Tokyo]), Person(2,Suzuki,None,2021-01-01T00:00+09:00[Asia/Tokyo]), Person(3,Sato,None,2021-01-01T00:00+09:00[Asia/Tokyo]))
