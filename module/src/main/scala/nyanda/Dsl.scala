package nyanda

import cats._
import cats.implicits._
import cats.data.Kleisli
import cats.effect.Sync

trait Dsl[F[_]: Sync]
    extends ResultSetGetInstances[F]
    with ResultSetReadInstances[F]
    with ParameterBindInstances[F]
    with SQLSyntax[F] {
  object DB extends DatabaseOps[F]
  object RS extends ResultSetOps[F]
}

object Dsl:
  def apply[F[_]: Sync]: Dsl[F] = new Dsl[F] {}

private[nyanda] trait DatabaseOps[F[_]: Sync]:

  def update(sql: SQL[F]): Query[F, Int] = Kleisli { conn =>
    new ConnectionOps[F](conn).update(sql)
  }

  def query[A](sql: SQL[F])(using g: ResultSetRead[F, A]): Kleisli[F, Connection[F], A] =
    Kleisli(new ConnectionOps[F](_).query(sql) >>= g.read)

private[nyanda] trait ResultSetOps[F[_]]:

  def get[A](column: String)(using g: ResultSetGet[F, A]): Kleisli[F, ResultSet[F], A] = Kleisli(g.get(column))

private[nyanda] class ConnectionOps[F[_]: Sync](connection: Connection[F]):

  def query[A](sql: SQL[F]): F[ResultSet[F]] =
    for {
      s <- connection.prepareStatement(sql.statement)
      _ <- bindParams(sql, s)
      rs <- s.executeQuery()
    } yield rs

  def update(sql: SQL[F]): F[Int] =
    for {
      s <- connection.prepareStatement(sql.statement)
      _ <- bindParams(sql, s)
      result <- s.executeUpdate()
    } yield result

  private def bindParams(sql: SQL[F], s: PreparedStatement[F]): F[Seq[Unit]] =
    sql.params.zipWithIndex.traverse { case (p, index) =>
      p.bind(s, index + 1)
    }
