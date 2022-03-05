package nyanda

import cats.*
import cats.implicits.*

trait ResultSetGet[F[_], A]:
  def get(column: String)(rs: ResultSet[F]): F[A]

object ResultSetGet:

  def apply[F[_], A](f: String => ResultSet[F] => F[A]): ResultSetGet[F, A] =
    new ResultSetGet[F, A]:
      def get(column: String)(rs: ResultSet[F]): F[A] = f(column)(rs)

  given [F[_]: Functor]: Functor[[A] =>> ResultSetGet[F, A]] with
    def map[A, B](fa: ResultSetGet[F, A])(f: A => B): ResultSetGet[F, B] =
      ResultSetGet(column => rs => fa.get(column)(rs).map(f))

  given javaSqlArrayResultSetGet[F[_]]: ResultSetGet[F, java.sql.Array] =
    ResultSetGet(column => rs => rs.getArray(column))

  given [F[_]]: ResultSetGet[F, Boolean] = ResultSetGet(column => rs => rs.getBoolean(column))

  given [F[_]]: ResultSetGet[F, Byte] = ResultSetGet(column => rs => rs.getByte(column))

  given byteArrayResultSetGet[F[_]]: ResultSetGet[F, Array[Byte]] = ResultSetGet(column => rs => rs.getBytes(column))

  given [F[_]]: ResultSetGet[F, String] = ResultSetGet(column => rs => rs.getString(column))

  given [F[_]]: ResultSetGet[F, Int] = ResultSetGet(column => rs => rs.getInt(column))

  given [F[_]]: ResultSetGet[F, Long] = ResultSetGet(column => rs => rs.getLong(column))

  given [F[_]]: ResultSetGet[F, Short] = ResultSetGet(column => rs => rs.getShort(column))

  given [F[_]]: ResultSetGet[F, Float] = ResultSetGet(column => rs => rs.getFloat(column))

  given [F[_]]: ResultSetGet[F, Double] = ResultSetGet(column => rs => rs.getDouble(column))

  given [F[_]]: ResultSetGet[F, java.sql.Timestamp] = ResultSetGet(column => rs => rs.getTimestamp(column))

  given [F[_]: Functor, T](using g: ResultSetGet[F, java.sql.Timestamp]): ResultSetGet[F, java.time.Instant] =
    g.map(_.toInstant)

  given [F[_]: Functor, T](using g: ResultSetGet[F, java.sql.Timestamp]): ResultSetGet[F, java.util.Date] =
    g.map(t => new java.util.Date(t.getTime))

  given [F[_]: Functor, T](using g: ResultSetGet[F, java.time.Instant]): ResultSetGet[F, java.time.ZonedDateTime] =
    g.map(i => java.time.ZonedDateTime.ofInstant(i, java.time.ZoneId.systemDefault))

  given [F[_]: Functor, T](using g: ResultSetGet[F, java.time.Instant]): ResultSetGet[F, java.time.LocalDateTime] =
    g.map(i => java.time.LocalDateTime.ofInstant(i, java.time.ZoneId.systemDefault))

  given [F[_]]: ResultSetGet[F, java.sql.Date] = ResultSetGet(column => rs => rs.getDate(column))

  given [F[_]: Functor, T](using g: ResultSetGet[F, java.sql.Date]): ResultSetGet[F, java.time.LocalDate] =
    g.map(_.toLocalDate)

  given [F[_]]: ResultSetGet[F, java.sql.Time] = ResultSetGet(column => rs => rs.getTime(column))

  given [F[_]: Functor, T](using g: ResultSetGet[F, T]): ResultSetGet[F, Option[T]] =
    g.map(Option.apply)
