package sjdbc.core.syntax

import sjdbc.core._
import javax.sql.DataSource
import java.sql.Connection

trait DataSourceSyntax:

  extension (dataSource: DataSource)
    def autoCommit(): Connection = new DataSourceOps(dataSource).autoCommit()
    def begin(): Connection = new DataSourceOps(dataSource).begin()
    def readOnly(): Connection = new DataSourceOps(dataSource).readOnly()
