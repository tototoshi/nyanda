# sjdbc

Extension methods for jdbc

```scala
import sjdbc.core._
import sjdbc.core.syntax._

Using.resource(dataSource.getConnection()) { conn =>
  people.foreach { p =>
    conn.update(sql"insert into person (id, name) values (${p.id}, ${p.name})")
  }
  val result1 = conn.query(sql"select id, name from person where id = ${1}").option(parser)
  val result2 = conn.query(sql"select id, name from person order by id").seq(parser)
}
```

## With Cats-Effect

```scala
import sjdbc.core._
import sjdbc.cats.effect._
import sjdbc.cats.effect.syntax._

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

dataSource.transaction[IO].use(queryGroup[IO].run).map(_ => ExitCode.Success)
```
