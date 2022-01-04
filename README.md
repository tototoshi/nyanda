# nyanda

Database Accessor for cats

```scala
// ./example/src/main/scala/nyanda/example/Main.scala

import nyanda._
import nyanda.syntax._

val reader = ResultSetReader { rs => Person(id = rs.int("id"), name = rs.string("name")) }

def queryGroup[F[_]: Sync: Console]: Kleisli[F, Connection, (Option[Person], Seq[Person])] =
  for {
    _ <- DB[F].update(sql"""
                  create table if not exists person(
                    id integer not null,
                    name varchar(32) not null,
                    primary key(id)
                  )
                  """)
    _ <- people.traverse { p =>
      DB[F].update(sql"insert into person (id, name) values (${p.id}, ${p.name})")
    }
    result1 <- DB[F].query(sql"select id, name from person where id = ${1}", reader.option)
    result2 <- DB[F].query(sql"select id, name from person", reader.seq)
    _ <- Kleisli.liftF(Console[F].println(result1))
    _ <- Kleisli.liftF(Console[F].println(result2))
  } yield (result1, result2)

override def run(args: List[String]): IO[ExitCode] =
  dataSource.transaction[IO].use(queryGroup[IO].run).map(_ => ExitCode.Success)
```
