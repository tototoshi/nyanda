package nyanda

import munit.FunSuite
import cats._
import cats.implicits._
import cats.effect._
import cats.effect.implicits._
import cats.effect.unsafe.implicits.global

class ResultSetGetTest extends FunSuite with Dsl[IO]:

  val mockResultSet: ResultSet[IO] = new TestResultSet[IO] {
    override def getInt(columnLabel: String) = IO.pure(1)
    override def getString(columnLabel: String) = IO.pure("s")
  }

  test("get single value") {
    assertEquals(RS.get[Option[Int]]("i")(mockResultSet).unsafeRunSync(), 1.some)
  }

  test("composition") {
    val g1 =
      for
        i <- RS.get[Int]("i")
        s <- RS.get[String]("s")
      yield (i, s)
    assertEquals(g1(mockResultSet).unsafeRunSync(), (1, "s"))

    val g2 = (RS.get[Int]("i"), RS.get[String]("s")).mapN((i, s) => (i, s))
    assertEquals(g2(mockResultSet).unsafeRunSync(), (1, "s"))
  }
