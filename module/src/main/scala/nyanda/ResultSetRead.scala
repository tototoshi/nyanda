package nyanda

import cats._
import cats.data.Kleisli
import cats.implicits._
import cats.effect._

trait ResultSetRead[F[_], T]:
  def read(rs: ResultSet[F]): F[T]

object ResultSetRead:
  def apply[F[_], T](k: Kleisli[F, ResultSet[F], T]): ResultSetRead[F, T] =
    new ResultSetRead[F, T] { def read(rs: ResultSet[F]): F[T] = k.run(rs) }

trait ResultSetReadInstances[F[_]: Sync]:

  given [T](using r: ResultSetRead[F, T]): ResultSetRead[F, Option[T]] with
    def read(rs: ResultSet[F]): F[Option[T]] =
      for {
        hasNext <- rs.next()
        result <- if (hasNext) r.read(rs).map(_.some) else Sync[F].pure(None)
      } yield result

  given [T](using r: ResultSetRead[F, T]): ResultSetRead[F, Seq[T]] with
    def read(rs: ResultSet[F]): F[Seq[T]] = Monad[F].whileM[Seq, T](rs.next())(r.read(rs))
