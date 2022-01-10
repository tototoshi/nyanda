package nyanda

import cats.implicits._
import cats.effect.Sync
import cats.data.Kleisli
import syntax.SQLSyntax

trait DatabaseOps[F[_]: Sync]:

  def update(sql: SQL[F]): Kleisli[F, Connection[F], Int] = Kleisli { conn =>
    new ConnectionOps[F](conn).update(sql)
  }

  def query[A](sql: SQL[F])(using g: ResultSetRead[F, A]): Kleisli[F, Connection[F], A] =
    Kleisli(new ConnectionOps[F](_).query(sql) >>= g.read)

trait ResultSetOps[F[_]]:

  def get[A](column: String)(using g: ResultSetGet[F, A]): Kleisli[F, ResultSet[F], A] = g.get(column)
