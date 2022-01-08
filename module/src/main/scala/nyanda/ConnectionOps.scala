package nyanda

import cats._
import cats.implicits._
import cats.effect.Sync
import java.sql.Connection
import java.sql.PreparedStatement

class ConnectionOps[F[_]: Sync: FlatMap](connection: Connection):

  def query[A](sql: SQL): F[ResultSet[F]] =
    for {
      s <- prepareStatement(sql.statement)
      _ <- bindParams(sql, s)
      rs <- executeQuery(s)
    } yield rs

  def update(sql: SQL): F[Int] =
    for {
      s <- prepareStatement(sql.statement)
      _ <- bindParams(sql, s)
      result <- executeUpdate(s)
    } yield result

  private def prepareStatement(s: String): F[PreparedStatement] = Sync[F].blocking(connection.prepareStatement(s))

  private def bindParams(sql: SQL, s: PreparedStatement): F[Unit] = Sync[F].blocking {
    sql.params.zipWithIndex.foreach { case (p, index) => p.bind(s, index + 1) }
  }

  private def executeQuery(s: PreparedStatement): F[ResultSet[F]] =
    Sync[F].delay(s.executeQuery()).map(rs => ResultSet[F](rs))

  private def executeUpdate(s: PreparedStatement): F[Int] = Sync[F].blocking(s.executeUpdate())
