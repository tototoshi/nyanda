package sjdbc.core

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class ConnectionOps(connection: Connection):

  def query[A](sql: SQL): ResultSet =
    val s = connection.prepareStatement(sql.statement)
    bindParams(sql, s)
    s.executeQuery()

  def update[A](sql: SQL): Int =
    val s = connection.prepareStatement(sql.statement)
    bindParams(sql, s)
    s.executeUpdate()

  private def bindParams(sql: SQL, s: PreparedStatement) =
    sql.params.zipWithIndex.foreach {
      case (p: Int, idx) => s.setInt(idx + 1, p)
      case (p: Long, idx) => s.setLong(idx + 1, p)
      case (p: Float, idx) => s.setFloat(idx + 1, p)
      case (p: String, idx) => s.setString(idx + 1, p)
      case (p: ParameterBinder, idx) => p.bind(s, idx + 1)
      case (p, _) => throw new ParameterBindException(s"Parameter bind failed. [param=$p]")
    }
