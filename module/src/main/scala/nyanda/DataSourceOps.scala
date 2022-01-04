package nyanda

import javax.sql.DataSource
import java.sql.ResultSet
import cats._
import cats.implicits._
import cats.effect._
import cats.effect.implicits._
import cats.effect.kernel.Sync
import cats.effect.kernel.Resource
import java.sql.Connection
import cats.effect.kernel.Resource.ExitCase

class DataSourceOps(dataSource: DataSource):

  def readOnly[F[_]: Sync]: Resource[F, Connection] =
    Resource
      .make(Sync[F].blocking {
        val conn = dataSource.getConnection()
        conn.setReadOnly(true)
        conn
      })(conn => Sync[F].blocking(conn.close()))

  def autoCommit[F[_]: Sync]: Resource[F, Connection] =
    Resource
      .make(Sync[F].blocking {
        val conn = dataSource.getConnection()
        conn.setReadOnly(false)
        conn.setAutoCommit(true)
        conn

      })(conn => Sync[F].blocking(conn.close()))

  def transaction[F[_]: Sync]: Resource[F, Connection] =
    Resource
      .makeCase(Sync[F].blocking {
        val conn = dataSource.getConnection()
        conn.setReadOnly(false)
        conn.setAutoCommit(false)
        conn
      }) {
        case (conn, ExitCase.Errored(e)) =>
          Sync[F].blocking(conn.rollback()) *> Sync[F].blocking(conn.close()) *> Sync[F].raiseError(e)
        case (conn, _) =>
          Sync[F].blocking(conn.commit()) *> Sync[F].blocking(conn.close())
      }
