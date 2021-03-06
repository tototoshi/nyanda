package nyanda.example

import cats.implicits.*
import cats.effect.*
import org.h2.jdbcx.JdbcDataSource
import nyanda.*

object HelloExample extends IOApp.Simple:

  import Dsl.IO._

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
