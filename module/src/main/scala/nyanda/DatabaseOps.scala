package nyanda

import cats.implicits.*
import cats.effect.kernel.Sync
import cats.data.Kleisli
import cats.Monad

trait DatabaseOps[F[_]]:

  def update(sql: SQL[F]): Query[F, Int]

  def query[A](sql: SQL[F])(using g: ResultSetConsume[F, A]): Kleisli[F, Connection[F], A]

object DatabaseOps:

  def apply[F[_]](using ev: DatabaseOps[F]) = ev

  given [F[_]: Monad]: DatabaseOps[F] with

    def update(sql: SQL[F]): Query[F, Int] = Kleisli { conn =>
      for
        s <- conn.prepareStatement(sql.statement)
        _ <- bindParams(sql, s)
        result <- s.executeUpdate()
      yield result
    }

    def query[A](sql: SQL[F])(using g: ResultSetConsume[F, A]): Kleisli[F, Connection[F], A] = Kleisli { conn =>
      val rs: F[ResultSet[F]] = for
        s <- conn.prepareStatement(sql.statement)
        _ <- bindParams(sql, s)
        rs <- s.executeQuery()
      yield rs

      rs >>= g.consume
    }

    private def bindParams(sql: SQL[F], s: PreparedStatement[F]): F[Seq[Unit]] =
      sql.params.zipWithIndex.traverse { case (p, index) =>
        p.bind(s, index + 1)
      }
