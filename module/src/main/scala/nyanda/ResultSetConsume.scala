package nyanda

import cats.*
import cats.implicits.*

trait ResultSetConsume[F[_], T]:

  def consume(rs: ResultSet[F]): F[T]

object ResultSetConsume:

  given [F[_], T](using r: ResultSetConsume[F, Option[T]], m: MonadError[F, Throwable]): ResultSetConsume[F, T] with
    def consume(rs: ResultSet[F]): F[T] =
      r.consume(rs).flatMap {
        case Some(v) => m.pure(v)
        case None => m.raiseError(new NoSuchElementException)
      }

  given [F[_]: Monad, T](using r: ResultSetRead[F, T]): ResultSetConsume[F, Option[T]] with
    def consume(rs: ResultSet[F]): F[Option[T]] =
      for
        hasNext <- rs.next()
        result <-
          if (hasNext) r.read(rs).map(_.some)
          else Monad[F].pure(None)
      yield result

  given [F[_]: Monad, T, S[_]: Traverse: Alternative](using r: ResultSetRead[F, T]): ResultSetConsume[F, S[T]] with
    def consume(rs: ResultSet[F]): F[S[T]] = Monad[F].whileM[S, T](rs.next())(r.read(rs))
