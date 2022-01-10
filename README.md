# nyanda

[![Scala CI](https://github.com/tototoshi/nyanda/actions/workflows/scala.yml/badge.svg)](https://github.com/tototoshi/nyanda/actions/workflows/scala.yml)

Database Accessor for cats

- [Example](./example/src/main/scala/nyanda/example/Main.scala)

## Usage


### Hello, World

```scala
package nyanda.example

import cats.implicits._
import cats.effect._
import org.h2.jdbcx.JdbcDataSource
import nyanda._

object Hello extends IOApp.Simple with Dsl[IO]:

  val dataSource =
    val ds = new JdbcDataSource()
    ds.setUrl("jdbc:h2:mem:hello;MODE=MySQL")
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
import nyanda._
object Main extends Dsl[cats.effect.IO]:
  ... your code
```

```scala
import nyanda._
val dsl: Dsl[IO] = new Dsl[IO]
import dsl{_, given}

...your code
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
