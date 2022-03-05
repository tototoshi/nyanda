package nyanda

import cats.*
import cats.data.Kleisli
import cats.implicits.*

trait ResultSetRead[F[_], T]:
  def read(rs: ResultSet[F]): F[T]

object ResultSetRead:
  def apply[F[_], T](k: Kleisli[F, ResultSet[F], T]): ResultSetRead[F, T] =
    new ResultSetRead[F, T] { def read(rs: ResultSet[F]): F[T] = k.run(rs) }
