package nyanda

import munit.FunSuite
import java.util.Date
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.LocalDate
import java.time.ZoneOffset
import cats.implicits._
import cats.effect.{IO, Ref}
import cats.effect.implicits._
import cats.effect.unsafe.implicits.global

class ParameterBinderTest extends FunSuite:

  private val dsl: Dsl[IO] = Dsl[IO]
  import dsl.{_, given}

  test("Bind Int") {
    val id = 100
    val sql = sql"select id from person where id = $id"

    assertEquals(sql.statement, "select id from person where id = ?")
    assertEquals(sql.params.size, 1)

    val program = for
      ref <- Ref[IO].of(0, 0)
      stmt = new MockPreparedStatement[IO] {
        override def setInt(parameterIndex: Int, x: Int): IO[Unit] = ref.update(_ => (parameterIndex, x))
      }
      _ <- sql.params.head.bind(stmt, 1)
      _ <- ref.get.map { case (parameterIndex, x) =>
        assertEquals(parameterIndex, 1)
        assertEquals(x, 100)
      }
    yield ()

    program.unsafeRunSync()
  }

  test("Bind Short") {
    val id: Short = 1
    val sql = sql"select id from person where id = $id"
    assertEquals(sql.statement, "select id from person where id = ?")
    assertEquals(sql.params.size, 1)

    val program = for
      ref <- Ref[IO].of(0, 0.toShort)
      stmt = new MockPreparedStatement[IO] {
        override def setShort(parameterIndex: Int, x: Short): IO[Unit] = ref.update(_ => (parameterIndex, x))
      }
      _ <- sql.params.head.bind(stmt, 1)
      _ <- ref.get.map { case (parameterIndex, x) =>
        assertEquals(parameterIndex, 1)
        assertEquals(x, id)
      }
    yield ()

    program.unsafeRunSync()
  }

  test("Bind Long") {
    val id = 1L
    val sql = sql"select id from person where id = $id"
    assertEquals(sql.statement, "select id from person where id = ?")
    assertEquals(sql.params.size, 1)

    val program = for
      ref <- Ref[IO].of(0, 0.toLong)
      stmt = new MockPreparedStatement[IO] {
        override def setLong(parameterIndex: Int, x: Long): IO[Unit] = ref.update(_ => (parameterIndex, x))
      }
      _ <- sql.params.head.bind(stmt, 1)
      _ <- ref.get.map { case (parameterIndex, x) =>
        assertEquals(parameterIndex, 1)
        assertEquals(x, id)
      }
    yield ()

    program.unsafeRunSync()
  }

  test("Bind String") {
    val id = "1"
    val sql = sql"select id from person where id = $id"
    assertEquals(sql.statement, "select id from person where id = ?")
    assertEquals(sql.params.size, 1)

    val program = for
      ref <- Ref[IO].of(0, "")
      stmt = new MockPreparedStatement[IO] {
        override def setString(parameterIndex: Int, x: String): IO[Unit] = ref.update(_ => (parameterIndex, x))
      }
      _ <- sql.params.head.bind(stmt, 1)
      _ <- ref.get.map { case (parameterIndex, x) =>
        assertEquals(parameterIndex, 1)
        assertEquals(x, id)
      }
    yield ()

    program.unsafeRunSync()
  }

  test("Bind java.sql.Timestamp") {
    val createdAt = new java.sql.Timestamp(0)
    val sql = sql"select id from person where created_at > ${createdAt}"
    assertEquals(sql.statement, "select id from person where created_at > ?")
    assertEquals(sql.params.size, 1)

    val program = for
      ref <- Ref[IO].of(0, null.asInstanceOf[java.sql.Timestamp])
      stmt = new MockPreparedStatement[IO] {
        override def setTimestamp(parameterIndex: Int, x: java.sql.Timestamp): IO[Unit] =
          ref.update(_ => (parameterIndex, x))
      }
      _ <- sql.params.head.bind(stmt, 1)
      _ <- ref.get.map { case (parameterIndex, x) =>
        assertEquals(parameterIndex, 1)
        assertEquals(x, createdAt)
      }
    yield ()

    program.unsafeRunSync()
  }

  test("Bind java.util.Date") {
    val createdAt = new Date(0)
    val sql = sql"select id from person where created_at > ${createdAt}"
    assertEquals(sql.statement, "select id from person where created_at > ?")
    assertEquals(sql.params.size, 1)

    val program = for
      ref <- Ref[IO].of(0, null.asInstanceOf[java.sql.Timestamp])
      stmt = new MockPreparedStatement[IO] {
        override def setTimestamp(parameterIndex: Int, x: java.sql.Timestamp): IO[Unit] =
          ref.update(_ => (parameterIndex, x))
      }
      _ <- sql.params.head.bind(stmt, 1)
      _ <- ref.get.map { case (parameterIndex, x) =>
        assertEquals(parameterIndex, 1)
        assertEquals(x, new java.sql.Timestamp(createdAt.getTime))
      }
    yield ()

    program.unsafeRunSync()
  }

  test("Bind java.time.Instant") {
    val createdAt = Instant.ofEpochMilli(0)
    val sql = sql"select id from person where created_at > ${createdAt}"
    assertEquals(sql.statement, "select id from person where created_at > ?")
    assertEquals(sql.params.size, 1)

    val program = for
      ref <- Ref[IO].of(0, null.asInstanceOf[java.sql.Timestamp])
      stmt = new MockPreparedStatement[IO] {
        override def setTimestamp(parameterIndex: Int, x: java.sql.Timestamp): IO[Unit] =
          ref.update(_ => (parameterIndex, x))
      }
      _ <- sql.params.head.bind(stmt, 1)
      _ <- ref.get.map { case (parameterIndex, x) =>
        assertEquals(parameterIndex, 1)
        assertEquals(x, java.sql.Timestamp.from(createdAt))
      }
    yield ()

    program.unsafeRunSync()
  }

  test("Bind java.time.LocalDateTime") {
    val createdAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault)
    val sql = sql"select id from person where created_at > ${createdAt}"
    assertEquals(sql.statement, "select id from person where created_at > ?")
    assertEquals(sql.params.size, 1)

    val program = for
      ref <- Ref[IO].of(0, null.asInstanceOf[java.sql.Timestamp])
      stmt = new MockPreparedStatement[IO] {
        override def setTimestamp(parameterIndex: Int, x: java.sql.Timestamp): IO[Unit] =
          ref.update(_ => (parameterIndex, x))
      }
      _ <- sql.params.head.bind(stmt, 1)
      _ <- ref.get.map { case (parameterIndex, x) =>
        assertEquals(parameterIndex, 1)
        assertEquals(x, java.sql.Timestamp.from(ZonedDateTime.of(createdAt, ZoneId.systemDefault).toInstant))
      }
    yield ()

    program.unsafeRunSync()
  }

  test("Bind java.time.ZonedDateTime") {
    val createdAt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault)
    val sql = sql"select id from person where created_at > ${createdAt}"
    assertEquals(sql.statement, "select id from person where created_at > ?")
    assertEquals(sql.params.size, 1)

    val program = for
      ref <- Ref[IO].of(0, null.asInstanceOf[java.sql.Timestamp])
      stmt = new MockPreparedStatement[IO] {
        override def setTimestamp(parameterIndex: Int, x: java.sql.Timestamp): IO[Unit] =
          ref.update(_ => (parameterIndex, x))
      }
      _ <- sql.params.head.bind(stmt, 1)
      _ <- ref.get.map { case (parameterIndex, x) =>
        assertEquals(parameterIndex, 1)
        assertEquals(x, java.sql.Timestamp.from(createdAt.toInstant))
      }
    yield ()

    program.unsafeRunSync()
  }

  test("Bind Option[T]") {
    val id1: Option[Int] = Some(1)
    val id2: Option[Int] = None
    val sql = sql"select id from person where id = $id1 or id = $id2"
    assertEquals(sql.statement, "select id from person where id = ? or id = ?")
    assertEquals(sql.params.size, 2)

    val program = for
      ref <- Ref[IO].of(Seq.empty[(Int, Any)])
      stmt = new MockPreparedStatement[IO] {
        override def setInt(parameterIndex: Int, x: Int): IO[Unit] =
          ref.update(xs => xs :+ (parameterIndex -> x))
        override def setObject(parameterIndex: Int, x: java.lang.Object): IO[Unit] =
          ref.update(xs => xs :+ (parameterIndex -> x))
      }
      _ <- sql.params(0).bind(stmt, 1)
      _ <- sql.params(1).bind(stmt, 2)
      _ <- ref.get.map { value =>
        assertEquals(Seq((1, 1), (2, null.asInstanceOf[Any])), value)
      }
    yield ()

    program.unsafeRunSync()
  }

  test("Bind Some[T]") {
    val id: Some[Int] = Some(1)
    val sql = sql"select id from person where id = $id"
    assertEquals(sql.statement, "select id from person where id = ?")
    assertEquals(sql.params.size, 1)

    val program = for
      ref <- Ref[IO].of(0, 0)
      stmt = new MockPreparedStatement[IO] {
        override def setInt(parameterIndex: Int, x: Int): IO[Unit] =
          ref.update(_ => (parameterIndex, x))
      }
      _ <- sql.params.head.bind(stmt, 1)
      _ <- ref.get.map { case (parameterIndex, x) =>
        assertEquals(parameterIndex, 1)
        assertEquals(Some(x), id)
      }
    yield ()

    program.unsafeRunSync()
  }

  test("Bind None") {
    val id: None.type = None
    val sql = sql"select id from person where id = $id"
    assertEquals(sql.statement, "select id from person where id = ?")
    assertEquals(sql.params.size, 1)

    val program = for
      ref <- Ref[IO].of(0, new java.lang.Object())
      stmt = new MockPreparedStatement[IO] {
        override def setObject(parameterIndex: Int, x: java.lang.Object): IO[Unit] =
          ref.update(_ => (parameterIndex, x))
      }
      _ <- sql.params.head.bind(stmt, 1)
      _ <- ref.get.map { case (parameterIndex, x) =>
        assertEquals(parameterIndex, 1)
        assertEquals(x, null)
      }
    yield ()

    program.unsafeRunSync()
  }
