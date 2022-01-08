package nyanda

import cats.data.Kleisli
import cats.effect.Sync
import java.sql.ResultSet

trait ResultSetGet[F[_], A]:
  def get(column: String): Kleisli[F, ResultSet, A]

object ResultSetGet:
  def apply[F[_], A](f: String => ResultSet => F[A]): ResultSetGet[F, A] =
    new ResultSetGet[F, A] {
      def get(column: String): Kleisli[F, ResultSet, A] = Kleisli(f(column))
    }

trait ResultSetGetInstances[F[_]: Sync]:

  implicit def stringGet: ResultSetGet[F, String] = ResultSetGet[F, String] { column => rs =>
    Sync[F].delay(rs.getString(column))
  }

  implicit def intGet: ResultSetGet[F, Int] = ResultSetGet[F, Int] { column => rs =>
    Sync[F].delay(rs.getInt(column))
  }
