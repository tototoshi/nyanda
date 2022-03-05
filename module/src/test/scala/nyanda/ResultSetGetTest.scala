package nyanda

import munit.FunSuite
import cats.*
import cats.implicits.*

class ResultSetGetTest extends FunSuite:

  private val dsl: Dsl[Id] = Dsl[Id]
  import dsl.*

  val mockResultSet: ResultSet[Id] = new TestResultSet[Id] {
    override def getInt(columnLabel: String) = 1
    override def getString(columnLabel: String) = "s"
  }

  test("get single value") {
    assertEquals(RS.get[Option[Int]]("i")(mockResultSet), 1.some)
  }

  test("composition") {
    val g1 =
      for
        i <- RS.get[Int]("i")
        s <- RS.get[String]("s")
      yield (i, s)
    assertEquals(g1(mockResultSet), (1, "s"))

    val g2 = (RS.get[Int]("i"), RS.get[String]("s")).mapN((i, s) => (i, s))
    assertEquals(g2(mockResultSet), (1, "s"))
  }
