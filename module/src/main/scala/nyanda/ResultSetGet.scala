package nyanda

import cats._
import cats.implicits._
import cats.effect.Sync

trait ResultSetGet[F[_], A]:
  def get(column: String)(rs: ResultSet[F]): F[A]

object ResultSetGet:

  def apply[F[_], A](f: String => ResultSet[F] => F[A]): ResultSetGet[F, A] =
    new ResultSetGet[F, A] {
      def get(column: String)(rs: ResultSet[F]): F[A] = f(column)(rs)
    }

trait ResultSetGetInstances[F[_]: Functor]:

  type ResultSetGetF[A] = ResultSetGet[F, A]

  given Functor[ResultSetGetF] with
    def map[A, B](fa: ResultSetGetF[A])(f: A => B): ResultSetGetF[B] =
      ResultSetGet(column => rs => fa.get(column)(rs).map(f))

  given ResultSetGet[F, String] = ResultSetGet(column => rs => rs.getString(column))

  given ResultSetGet[F, Int] = ResultSetGet(column => rs => rs.getInt(column))

  given ResultSetGet[F, Long] = ResultSetGet(column => rs => rs.getLong(column))

  given ResultSetGet[F, Short] = ResultSetGet(column => rs => rs.getShort(column))

  given ResultSetGet[F, java.sql.Timestamp] = ResultSetGet(column => rs => rs.getTimestamp(column))

  given [T](using g: ResultSetGet[F, java.sql.Timestamp]): ResultSetGet[F, java.time.Instant] = g.map(_.toInstant)

  given [T](using g: ResultSetGet[F, java.sql.Timestamp]): ResultSetGet[F, java.util.Date] =
    g.map(t => new java.util.Date(t.getTime))

  given [T](using g: ResultSetGet[F, java.time.Instant]): ResultSetGet[F, java.time.ZonedDateTime] =
    g.map(i => java.time.ZonedDateTime.ofInstant(i, java.time.ZoneId.systemDefault))

  given [T](using g: ResultSetGet[F, java.time.Instant]): ResultSetGet[F, java.time.LocalDateTime] =
    g.map(i => java.time.LocalDateTime.ofInstant(i, java.time.ZoneId.systemDefault))

  given ResultSetGet[F, java.sql.Date] = ResultSetGet(column => rs => rs.getDate(column))

  given [T](using g: ResultSetGet[F, java.sql.Date]): ResultSetGet[F, java.time.LocalDate] = g.map(_.toLocalDate)

  given [T](using g: ResultSetGet[F, T]): ResultSetGet[F, Option[T]] =
    g.map(Option.apply)
