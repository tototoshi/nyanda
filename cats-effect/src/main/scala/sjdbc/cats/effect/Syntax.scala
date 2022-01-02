package sjdbc.cats.effect

import javax.sql.DataSource
import cats.effect._

trait Syntax:
  extension (dataSource: DataSource) def transaction[F[_]: Sync] = new DataSourceOps(dataSource).transaction[F]
  extension (dataSource: DataSource) def readOnly[F[_]: Sync] = new DataSourceOps(dataSource).readOnly[F]
  extension (dataSource: DataSource) def autoCommit[F[_]: Sync] = new DataSourceOps(dataSource).autoCommit[F]
