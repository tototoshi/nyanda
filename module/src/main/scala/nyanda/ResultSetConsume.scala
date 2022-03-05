package nyanda

import cats._
import cats.implicits._
import cats.effect._

trait ResultSetConsume[F[_], T]:
  def consume(rs: ResultSet[F]): F[T]

trait ResultSetConsumeInstances:

  given [F[_]: Sync, T](using r: ResultSetConsume[F, Option[T]]): ResultSetConsume[F, T] with
    def consume(rs: ResultSet[F]): F[T] =
      r.consume(rs).flatMap {
        case Some(v) => Sync[F].pure(v)
        case None => Sync[F].raiseError(new NoSuchElementException)
      }

  given [F[_]: Sync, T](using r: ResultSetRead[F, T]): ResultSetConsume[F, Option[T]] with
    def consume(rs: ResultSet[F]): F[Option[T]] =
      for
        hasNext <- rs.next()
        result <-
          if (hasNext) r.read(rs).map(_.some)
          else Sync[F].pure(None)
      yield result

  given [F[_]: Monad, T, S[_]: Traverse: Alternative](using r: ResultSetRead[F, T]): ResultSetConsume[F, S[T]] with
    def consume(rs: ResultSet[F]): F[S[T]] = Monad[F].whileM[S, T](rs.next())(r.read(rs))
