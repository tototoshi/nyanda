package sjdbc.cats.effect

import sjdbc.core._
import java.sql.Connection
import java.sql.ResultSet
import cats.effect.kernel.Sync
import cats.data.Kleisli

trait Dsl[F[_]] {
  def update(sql: SQL): Kleisli[F, Connection, Int]
  def query(sql: SQL): Kleisli[F, Connection, ResultSet]
  def option[A](parser: ResultSetParser[A]): Kleisli[F, ResultSet, Option[A]]
  def seq[A](parser: ResultSetParser[A]): Kleisli[F, ResultSet, Seq[A]]
}

object Dsl {

  def apply[F[_]](implicit ev: Dsl[F]) = ev

  implicit def impl[F[_]: Sync]: Dsl[F] = new Dsl[F] {
    def update(sql: SQL): Kleisli[F, Connection, Int] = Kleisli { conn =>
      Sync[F].blocking(new ConnectionOps(conn).update(sql))
    }
    def query(sql: SQL): Kleisli[F, Connection, ResultSet] = Kleisli { conn =>
      Sync[F].blocking(new ConnectionOps(conn).query(sql))
    }

    def option[A](parser: ResultSetParser[A]): Kleisli[F, ResultSet, Option[A]] = Kleisli { rs =>
      Sync[F].blocking(new ResultSetOps(rs).option(parser))
    }

    def seq[A](parser: ResultSetParser[A]): Kleisli[F, ResultSet, Seq[A]] = Kleisli { rs =>
      Sync[F].blocking(new ResultSetOps(rs).seq(parser))
    }
  }

}
