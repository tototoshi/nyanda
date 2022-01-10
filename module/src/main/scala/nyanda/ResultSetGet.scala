package nyanda

import cats._
import cats.implicits._
import cats.data.Kleisli
import cats.effect.Sync

trait ResultSetGet[F[_], A]:
  def get(column: String): Kleisli[F, ResultSet[F], A]

object ResultSetGet:

  def apply[F[_], A](f: String => ResultSet[F] => F[A]): ResultSetGet[F, A] =
    new ResultSetGet[F, A] {
      def get(column: String): Kleisli[F, ResultSet[F], A] = Kleisli(f(column))
    }

trait ResultSetGetInstances[F[_]: Functor]:

  type ResultSetGetF[A] = ResultSetGet[F, A]

  given Functor[ResultSetGetF] with
    def map[A, B](fa: ResultSetGetF[A])(f: A => B): ResultSetGetF[B] = new ResultSetGet[F, B] {
      def get(column: String): Kleisli[F, ResultSet[F], B] =
        fa.get(column).map(f)
    }

  given ResultSetGet[F, String] with
    def get(column: String) = Kleisli(_.getString(column))

  given ResultSetGet[F, Int] with
    def get(column: String) = Kleisli(_.getInt(column))

  given ResultSetGet[F, Long] with
    def get(column: String) = Kleisli(_.getLong(column))

  given ResultSetGet[F, Short] with
    def get(column: String) = Kleisli(_.getShort(column))

  given ResultSetGet[F, java.sql.Timestamp] with
    def get(column: String) = Kleisli(_.getTimestamp(column))

  given [T](using g: ResultSetGet[F, java.sql.Timestamp]): ResultSetGet[F, java.time.Instant] = g.map(_.toInstant)

  given [T](using g: ResultSetGet[F, java.sql.Timestamp]): ResultSetGet[F, java.util.Date] =
    g.map(t => new java.util.Date(t.getTime))

  given [T](using g: ResultSetGet[F, java.time.Instant]): ResultSetGet[F, java.time.ZonedDateTime] =
    g.map(i => java.time.ZonedDateTime.ofInstant(i, java.time.ZoneId.systemDefault))

  given [T](using g: ResultSetGet[F, java.time.Instant]): ResultSetGet[F, java.time.LocalDateTime] =
    g.map(i => java.time.LocalDateTime.ofInstant(i, java.time.ZoneId.systemDefault))

  given [T](using g: ResultSetGet[F, T]): ResultSetGet[F, Option[T]] =
    g.map(Option.apply)
