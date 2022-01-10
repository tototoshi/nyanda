package nyanda

import cats._
import cats.implicits._
import cats.effect.Sync

trait Connection[F[_]]:
  def prepareStatement(sql: String): F[PreparedStatement[F]]
  def setReadOnly(readOnly: Boolean): F[Unit]
  def setAutoCommit(autoCommit: Boolean): F[Unit]
  def close(): F[Unit]
  def commit(): F[Unit]
  def rollback(): F[Unit]

object Connection:

  def apply[F[_]: Sync](conn: java.sql.Connection): Connection[F] = new Connection[F]:
    def prepareStatement(sql: String): F[PreparedStatement[F]] =
      Sync[F].blocking(conn.prepareStatement(sql)).map(s => PreparedStatement[F](s))
    def setReadOnly(readOnly: Boolean): F[Unit] =
      Sync[F].blocking(conn.setReadOnly(readOnly)).map(_ => ())
    def setAutoCommit(autoCommit: Boolean): F[Unit] =
      Sync[F].blocking(conn.setAutoCommit(autoCommit))
    def close(): F[Unit] =
      Sync[F].blocking(conn.close())
    def commit(): F[Unit] =
      Sync[F].blocking(conn.commit())
    def rollback(): F[Unit] =
      Sync[F].blocking(conn.rollback())
