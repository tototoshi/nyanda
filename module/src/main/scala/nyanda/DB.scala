package nyanda

import java.sql.Connection
import java.sql.ResultSet
import cats.effect.kernel.Sync
import cats.data.Kleisli

trait DB[F[_]] {
  def update(sql: SQL): Kleisli[F, Connection, Int]
  def query[A](sql: SQL, reader: ResultSetReader[A]): Kleisli[F, Connection, A]
}

object DB {

  def apply[F[_]](implicit ev: DB[F]) = ev

  implicit def impl[F[_]: Sync]: DB[F] = new DB[F] {

    def update(sql: SQL): Kleisli[F, Connection, Int] = Kleisli { conn =>
      new ConnectionOps[F](conn).update(sql)
    }
    def query[A](sql: SQL, reader: ResultSetReader[A]): Kleisli[F, Connection, A] = Kleisli { conn =>
      new ConnectionOps[F](conn).query(sql, reader)
    }

  }

}
