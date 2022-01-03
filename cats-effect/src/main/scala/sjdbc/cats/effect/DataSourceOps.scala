package sjdbc.cats.effect

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

  private val dataSourceOps = new sjdbc.core.DataSourceOps(dataSource)

  def readOnly[F[_]: Sync]: Resource[F, Connection] =
    Resource
      .make(Sync[F].blocking(dataSourceOps.readOnly()))(conn => Sync[F].blocking(conn.close()))

  def autoCommit[F[_]: Sync]: Resource[F, Connection] =
    Resource
      .make(Sync[F].blocking(dataSourceOps.autoCommit()))(conn => Sync[F].blocking(conn.close()))

  def transaction[F[_]: Sync]: Resource[F, Connection] =
    Resource
      .makeCase(Sync[F].blocking(dataSourceOps.begin())) {
        case (conn, ExitCase.Errored(e)) =>
          Sync[F].blocking(conn.rollback()) *> Sync[F].blocking(conn.close()) *> Sync[F].raiseError(e)
        case (conn, _) =>
          Sync[F].blocking(conn.commit()) *> Sync[F].blocking(conn.close())
      }
