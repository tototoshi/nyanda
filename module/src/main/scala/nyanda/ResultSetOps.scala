package nyanda

import cats.data.Kleisli
import cats.effect.kernel.Sync

trait ResultSetOps[F[_]]:

  def get[A](column: String)(using g: ResultSetGet[F, A]): Kleisli[F, ResultSet[F], A]

object ResultSetOps:

  def apply[F[_]](using ev: ResultSetOps[F]): ResultSetOps[F] = ev

  given [F[_]]: ResultSetOps[F] with
    def get[A](column: String)(using g: ResultSetGet[F, A]): Kleisli[F, ResultSet[F], A] = Kleisli(g.get(column))
