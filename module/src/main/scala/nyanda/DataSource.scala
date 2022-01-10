package nyanda

import cats.implicits._
import cats.effect.Sync

trait DataSource[F[_]]:
  def getConnection: F[Connection[F]]

object DataSource:
  def apply[F[_]: Sync](dataSource: javax.sql.DataSource): DataSource[F] = new DataSource[F]:
    def getConnection: F[Connection[F]] = Sync[F].blocking(dataSource.getConnection).map(c => Connection[F](c))
