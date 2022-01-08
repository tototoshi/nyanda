package nyanda

import cats.data.Kleisli
import cats.effect.Sync

trait ResultSetGet[F[_], A]:
  def get(column: String): Kleisli[F, ResultSet[F], A]

object ResultSetGet:
  def apply[F[_], A](f: String => ResultSet[F] => F[A]): ResultSetGet[F, A] =
    new ResultSetGet[F, A] {
      def get(column: String): Kleisli[F, ResultSet[F], A] = Kleisli(f(column))
    }

trait ResultSetGetInstances[F[_]: Sync]:

  implicit def stringGet: ResultSetGet[F, String] = ResultSetGet[F, String] { column => rs =>
    rs.getString(column)
  }

  implicit def intGet: ResultSetGet[F, Int] = ResultSetGet[F, Int] { column => rs =>
    rs.getInt(column)
  }
