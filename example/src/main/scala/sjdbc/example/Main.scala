package sjdbc.example

import sjdbc.core._
import sjdbc.cats.effect._
import cats._
import cats.data.Kleisli
import cats.implicits._
import cats.effect._
import cats.effect.kernel.Resource
import cats.effect.std.Console
import java.sql.Connection
import javax.sql.DataSource
import org.h2.jdbcx.JdbcDataSource

object Main extends IOApp:

  val dataSource =
    val ds = new JdbcDataSource()
    ds.setUrl("jdbc:h2:mem:example")
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

  val parser = ResultSetParser { rs => Person(id = rs.int("id"), name = rs.string("name")) }

  def queryGroup[F[_]: Sync: Console]: Kleisli[F, Connection, (Option[Person], Seq[Person])] =
    val dsl = Dsl[F]
    import dsl._

    for {
      _ <- update(sql"""
                    create table if not exists person(
                      id integer not null,
                      name varchar(32) not null,
                      primary key(id)
                    )
                    """)
      _ <- people.traverse { p =>
        update(sql"insert into person (id, name) values (${p.id}, ${p.name})")
      }
      result1 <- query(sql"select id, name from person where id = ${1}") >>> option(parser)
      result2 <- query(sql"select id, name from person") >>> seq(parser)
      _ <- Kleisli.liftF(Console[F].println(result1))
      _ <- Kleisli.liftF(Console[F].println(result2))
    } yield (result1, result2)

  override def run(args: List[String]): IO[ExitCode] =
    dataSource.transaction[IO].use(queryGroup[IO].run).map(_ => ExitCode.Success)

// sbt:root> example/run
// [info] compiling 1 Scala source to /Users/toshi/tmp/sjdbc/example/target/scala-3.1.0/classes ...
// [info] running (fork) sjdbc.example.Main
// [info] Some(Person(1,Takahashi))
// [info] List(Person(1,Takahashi), Person(2,Suzuki), Person(3,Sato))
