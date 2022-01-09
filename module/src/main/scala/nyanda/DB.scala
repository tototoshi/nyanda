package nyanda

import cats.effect.kernel.Sync
import cats.data.Kleisli
import syntax.SQLSyntax

trait DB[F[_]]
    extends ResultSetGetInstances[F]
    with ResultSetReadInstances[F]
    with SQLSyntax[F]
    with ParameterBindableInstances[F] {
  def update(sql: SQL[F]): Kleisli[F, Connection[F], Int]
  def query[A](sql: SQL[F]): Kleisli[F, Connection[F], ResultSet[F]]
  def as[A](implicit g: ResultSetRead[F, A]): Kleisli[F, ResultSet[F], A]
  def get[A](column: String)(implicit g: ResultSetGet[F, A]): Kleisli[F, ResultSet[F], A]
}

object DB {

  def apply[F[_]](implicit ev: DB[F]) = ev

  implicit def impl[F[_]: Sync]: DB[F] = new DB[F] {

    def update(sql: SQL[F]): Kleisli[F, Connection[F], Int] = Kleisli { conn =>
      new ConnectionOps[F](conn).update(sql)
    }

    def query[A](sql: SQL[F]): Kleisli[F, Connection[F], ResultSet[F]] = Kleisli { conn =>
      new ConnectionOps[F](conn).query(sql)
    }

    def as[A](implicit g: ResultSetRead[F, A]): Kleisli[F, ResultSet[F], A] = Kleisli(g.read)

    def get[A](column: String)(implicit g: ResultSetGet[F, A]): Kleisli[F, ResultSet[F], A] = g.get(column)

  }

}
