package nyanda

import cats._
import cats.implicits._
import cats.effect._
import cats.effect.implicits._
import cats.effect.kernel.Resource.ExitCase

class Transactor[F[_]: Sync] private (dataSource: DataSource[F]):

  def readOnly: Resource[F, Connection[F]] = connectionResource(acquireReadOnlyConnection)

  def autoCommit: Resource[F, Connection[F]] = connectionResource(acquireAutoCommitConnection)

  def transaction: Resource[F, Connection[F]] =
    for {
      c <- connectionResource(acquireTransaction)
      t <- Resource.makeCase(Sync[F].pure(c)) {
        case (conn, ExitCase.Errored(e)) =>
          conn.rollback() *> Sync[F].raiseError(e)
        case (conn, _) =>
          conn.commit()
      }
    } yield t

  private def connectionResource(acquire: => F[Connection[F]]) = Resource.make(acquire)(conn => conn.close())

  private def acquireReadOnlyConnection =
    for {
      conn <- dataSource.getConnection
      _ <- conn.setReadOnly(true)
    } yield conn

  private def acquireAutoCommitConnection =
    for {
      conn <- dataSource.getConnection
      _ <- conn.setReadOnly(false)
      _ <- conn.setAutoCommit(true)
    } yield conn

  private def acquireTransaction =
    for {
      conn <- dataSource.getConnection
      _ <- conn.setReadOnly(false)
      _ <- conn.setAutoCommit(false)
    } yield conn

object Transactor:
  def apply[F[_]: Sync](dataSource: javax.sql.DataSource): Transactor[F] = new Transactor[F](DataSource[F](dataSource))
