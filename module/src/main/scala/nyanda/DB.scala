package nyanda

import java.sql.Connection
import java.sql.ResultSet
import cats.effect.kernel.Sync
import cats.data.Kleisli

trait DB[F[_]] extends ResultSetGetInstances[F] with ResultSetReadInstances[F] {
  def update(sql: SQL): Kleisli[F, Connection, Int]
  def query[A](sql: SQL): Kleisli[F, Connection, ResultSet]
  def as[A](implicit g: ResultSetRead[F, A]): Kleisli[F, ResultSet, A]
  def get[A](column: String)(implicit g: ResultSetGet[F, A]): Kleisli[F, ResultSet, A]
}

object DB {

  def apply[F[_]](implicit ev: DB[F]) = ev

  implicit def impl[F[_]: Sync]: DB[F] = new DB[F] {

    def update(sql: SQL): Kleisli[F, Connection, Int] = Kleisli { conn =>
      new ConnectionOps[F](conn).update(sql)
    }

    def query[A](sql: SQL): Kleisli[F, Connection, ResultSet] = Kleisli { conn =>
      new ConnectionOps[F](conn).query(sql)
    }

    def as[A](implicit g: ResultSetRead[F, A]): Kleisli[F, ResultSet, A] = Kleisli(g.read)

    def get[A](column: String)(implicit g: ResultSetGet[F, A]): Kleisli[F, ResultSet, A] = g.get(column)

  }

}
