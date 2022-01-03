package sjdbc.core

import java.sql.Connection
import java.sql.ResultSet
import java.sql.PreparedStatement

class ConnectionOps(connection: Connection):

  def query(sql: SQL): ResultSet =
    val s = connection.prepareStatement(sql.statement)
    bindParams(sql, s)
    s.executeQuery()

  def update(sql: SQL): Int =
    val s = connection.prepareStatement(sql.statement)
    bindParams(sql, s)
    s.executeUpdate()

  private def bindParams(sql: SQL, s: PreparedStatement) =
    sql.params.zipWithIndex.foreach { case (p, index) => p.bind(s, index + 1) }
