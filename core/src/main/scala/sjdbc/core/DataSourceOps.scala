package sjdbc.core

import javax.sql.DataSource
import java.sql.Connection

class DataSourceOps(dataSource: DataSource):

  def autoCommit(): Connection =
    val conn = dataSource.getConnection
    conn.setReadOnly(false)
    conn.setAutoCommit(true)
    conn

  def begin(): Connection =
    val conn = dataSource.getConnection
    conn.setReadOnly(false)
    conn.setAutoCommit(false)
    conn

  def readOnly(): Connection =
    val conn = dataSource.getConnection
    conn.setReadOnly(true)
    conn.setAutoCommit(false)
    conn
