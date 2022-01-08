package nyanda

import javax.sql.DataSource
import java.sql.ResultSet
import cats._
import cats.implicits._
import cats.effect._
import cats.effect.implicits._
import cats.effect.kernel.Sync
import cats.effect.kernel.Resource
import cats.effect.kernel.Resource.ExitCase

class DataSourceOps(dataSource: DataSource):

  def readOnly[F[_]: Sync]: Resource[F, Connection[F]] =
    Resource
      .make(for {
        c <- Sync[F].blocking(dataSource.getConnection())
        conn = Connection[F](c)
        _ <- conn.setReadOnly(true)
      } yield conn)(conn => conn.close())

  def autoCommit[F[_]: Sync]: Resource[F, Connection[F]] =
    Resource
      .make(for {
        c <- Sync[F].blocking(dataSource.getConnection())
        conn = Connection[F](c)
        _ <- conn.setReadOnly(false)
        _ <- conn.setAutoCommit(true)
      } yield conn)(conn => conn.close())

  def transaction[F[_]: Sync]: Resource[F, Connection[F]] =
    Resource
      .makeCase(for {
        c <- Sync[F].blocking(dataSource.getConnection())
        conn = Connection[F](c)
        _ <- conn.setReadOnly(false)
        _ <- conn.setAutoCommit(false)
      } yield conn) {
        case (conn, ExitCase.Errored(e)) =>
          conn.rollback() *> conn.close() *> Sync[F].raiseError(e)
        case (conn, _) =>
          conn.commit() *> conn.close()
      }
