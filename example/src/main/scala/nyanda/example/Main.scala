package nyanda.example

import nyanda._
import cats._
import cats.data.Kleisli
import cats.implicits._
import cats.effect._
import cats.effect.kernel.Resource
import cats.effect.std.Console
import org.h2.jdbcx.JdbcDataSource

case class Person(id: Int, name: String, nickname: Option[String])

trait PersonDao[F[_]: Applicative] extends Dsl[F]:

  private val personGet =
    (RS.get[Int]("id"), RS.get[String]("name"), RS.get[Option[String]]("nickname")).mapN(Person.apply)

  given ResultSetRead[F, Person] = ResultSetRead(personGet)

  private val ddl: SQL[F] =
    sql"""
      create table if not exists person(
        id integer not null,
        name varchar(32) not null,
        nickname varchar(32),
        primary key(id)
      )
      """

  def createTable: QueryF[F, Int] = DB.update(ddl)

  def insert(p: Person): QueryF[F, Int] =
    DB.update(sql"insert into person (id, name, nickname) values (${p.id}, ${p.name}, ${p.nickname})")

  def findById(id: Int): QueryF[F, Option[Person]] =
    DB.query(sql"select id, name, nickname from person where id = ${1}")

  def findAll: QueryF[F, Seq[Person]] = DB.query(sql"select id, name, nickname from person")

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
      Person(1, "Takahashi", "Taka".some),
      Person(2, "Suzuki", None),
      Person(3, "Sato", None)
    )

  def personDao[F[_]: Sync] = new PersonDao[F] {}

  def queryGroup[F[_]: Sync: Console]: QueryF[F, (Option[Person], Seq[Person])] =
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
// [info] running (fork) nyanda.example.Main
// [info] Some(Person(1,Takahashi,Some(Taka)))
// [info] List(Person(1,Takahashi,Some(Taka)), Person(2,Suzuki,None), Person(3,Sato,None))
