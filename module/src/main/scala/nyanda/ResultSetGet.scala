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
