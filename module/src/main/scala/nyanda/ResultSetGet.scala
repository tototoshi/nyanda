package nyanda

import cats._
import cats.implicits._
import cats.data.Kleisli
import cats.effect.Sync

trait ResultSetGet[F[_]: Functor, A]:
  self =>

  def get(column: String): Kleisli[F, ResultSet[F], A]

  def map[B](f: A => B): ResultSetGet[F, B] = new ResultSetGet[F, B] {
    def get(column: String): Kleisli[F, ResultSet[F], B] =
      self.get(column).map(f)
  }

object ResultSetGet:
  def apply[F[_]: Functor, A](f: String => ResultSet[F] => F[A]): ResultSetGet[F, A] =
    new ResultSetGet[F, A] {
      def get(column: String): Kleisli[F, ResultSet[F], A] = Kleisli(f(column))
    }

trait ResultSetGetInstances[F[_]: Sync]:

  implicit def stringGet: ResultSetGet[F, String] = ResultSetGet { column => rs =>
    rs.getString(column)
  }

  implicit def intGet: ResultSetGet[F, Int] = ResultSetGet { column => rs =>
    rs.getInt(column)
  }

  implicit def longGet: ResultSetGet[F, Long] = ResultSetGet { column => rs =>
    rs.getLong(column)
  }

  implicit def shortGet: ResultSetGet[F, Short] = ResultSetGet { column => rs =>
    rs.getShort(column)
  }

  implicit def optionGet[T](implicit g: ResultSetGet[F, T]): ResultSetGet[F, Option[T]] =
    g.map(Option.apply)
