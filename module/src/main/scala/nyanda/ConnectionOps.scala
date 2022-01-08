package nyanda

import cats._
import cats.implicits._
import cats.effect.Sync

class ConnectionOps[F[_]: Sync](connection: Connection[F]):

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
