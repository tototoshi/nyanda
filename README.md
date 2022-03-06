# nyanda 🐱

[![Scala CI](https://github.com/tototoshi/nyanda/actions/workflows/scala.yml/badge.svg)](https://github.com/tototoshi/nyanda/actions/workflows/scala.yml)

Database Accessor for cats. Written in Scala 3 and cats-effect 3.

- [HelloExample](./example/src/main/scala/nyanda/example/HelloExample.scala)
- [DaoExample](./example/src/main/scala/nyanda/example/DaoExample.scala)

## Usage


### Hello, World

```scala
package nyanda.example

import cats.implicits.*
import cats.effect.*
import org.h2.jdbcx.JdbcDataSource
import nyanda.*

object Hello extends IOApp.Simple with Dsl.IO:

  val dataSource =
    val ds = new JdbcDataSource()
    ds.setUrl("jdbc:h2:mem:hello;DB_CLOSE_DELAY=1")
    ds.setUser("sa")
    ds.setPassword("")
    ds

  // "Transactor" is an utility to manage the database connection
  val transactor = Transactor[IO](dataSource)

  val run =
    // Declare a instance of "ResultSetRead" Type class
    given ResultSetRead[IO, String] = ResultSetRead(RS.get[String]("hello"))

    // Declare a query
    val q: Query[IO, String] = DB.query[String](sql"select 'Hello, World!' as hello")

    // Execute the query
    transactor.readOnly.use(q.run) >>= IO.println // => Hello, World!
```

### Import

You can use features by inheriting or importing the Dsl traits.

```scala
import nyanda.*
object Main extends Dsl.IO:
// ... your code
```

```scala
import nyanda.*
import Dsl.IO._

// ...your code
```

### Query

Write queries with string interpolation.

```scala
val id = 1
val q = sql"select * from person where id = ${id}"
```

and execute the queries.

```scala
// Equivalent to java.sql.Connection#executeUpdate
DB.update(sql"create table ....")

// Equivalent to java.sql.Connection#executeQuery
DB.query[Option[String]](sql"select * from ....")
```

### ResultSetGet[F, T]/ResultSetRead[F, T]

For the conversion from `ResultSet` to user-defined types, the `ResultSetGet[F, T]/ResultSetRead[F, T]` typeclasses are provided.

ResultSetGet is defined as follows.

```scala
trait ResultSetGet[F[_], T]:
  def get(column: String)(rs: ResultSet[F]): F[T]
```

`ResultSet[F]` is a type that wraps `java.sql.ResultSet` and is defined by this library to separate the "effects". Similarly, this library also defines `Connection[F]`, `DataSource[F]`, etc.


If you have an instance of `ResultSetGet[F, T]` (e.g. `ResultSetGet[IO, String]`), you can use the `RS.get[T]` method. (For simple types such as `String`, `Int`, `ZonedDateTime`, etc., an instance of `ResultSetGet[F, T]` is already provided.
)

```scala
def get[A](column: String)(using g: ResultSetGet[F, A]): Kleisli[F, ResultSet[F], A]
```

`get[T]` returns `Kleisli[F, ResultSet[F], A]`. you can use the features of `Kleisli`, `Applicative`, and `Monad` to compose operations with `get[T]`.
 You can then wrap it with `ResultSetRead.apply` to get an instance of `ResultSetRead[F, T]`.

```scala
case class Person(id: Int, name: String, nickname: Option[String])

def personGet[F[_]: Monad]: Kleisli[F, ResultSet[F], Person] = for
  id <- RS.get[Int]("id")
  name <- RS.get[String]("name")
  nickname <- RS.get[Option[Stirng]]("nickname")
yield Person(id, name, nickname)

/*
or

def personGet[F[_]: Applicative]: Kleisli[F, ResultSet[F], Person] =
  (RS.get[String]("id"), RS.get[String]("name"), RS.get("nickname")[Option[String]).mapN(Person.apply)
*/
```

### Connection and Transaction

`Transactor` is used to manage transactions. `Transactor` provides methods such as `autoCommit`, `readOnly`, and `transaction`.

```scala
import nyanda
import org.h2.jdbcx.JdbcDataSource

val dsl: Dsl[IO] = new Dsl[IO]
import dsl.*

val ds = new JdbcDataSource()
ds.setUrl("jdbc:h2:mem:hello;DB_CLOSE_DELAY=1")
ds.setUser("sa")
ds.setPassword("")

val transactor = Transactor[IO](ds)

val q = DB.query[Option[Person]](sql"select * from person")
transactor.transaction.useKleisli(q).unsafeRunSync()
```

You can also compose multiple queries and execute them at once.

The `DB.update` and `DB.query` methods return `Query[F, T]` type, which is an alias to `Kleisli[F, Connection[F], T]`.

So you can:

```scala
val insertAndFind: Query[IO, Option[Person]] =
  for
    _ <- DB.update(sql"insert into person (id, name, nickname, created_at) values (${p.id}, ${p.name}, ${p.nickname}, ${p.createdAt})")
    result <- DB.query[Option[Person]](sql"select id, name, nickname, created_at from person where id = ${1}")
  yield result

transactor.transaction.useKelisli(insertAndFind).unsafeRunSync()
```
