package nyanda

import cats.effect.Sync
import cats.data.Kleisli
import syntax.SQLSyntax

trait DatabaseOps[F[_]: Sync]:

  def update(sql: SQL[F]): Kleisli[F, Connection[F], Int] = Kleisli { conn =>
    new ConnectionOps[F](conn).update(sql)
  }

  def query[A](sql: SQL[F]): Kleisli[F, Connection[F], ResultSet[F]] = Kleisli { conn =>
    new ConnectionOps[F](conn).query(sql)
  }

  def as[A](using g: ResultSetRead[F, A]): Kleisli[F, ResultSet[F], A] = Kleisli(g.read)

  def get[A](column: String)(using g: ResultSetGet[F, A]): Kleisli[F, ResultSet[F], A] = g.get(column)
