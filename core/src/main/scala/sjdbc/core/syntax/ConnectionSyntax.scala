package sjdbc.core.syntax

import sjdbc.core._
import java.sql.Connection
import java.sql.ResultSet

trait ConnectionSyntax:
  extension (connection: Connection)
    def query[A](stmt: SQL): ResultSet = new ConnectionOps(connection).query(stmt)
    def update[A](stmt: SQL): Int = new ConnectionOps(connection).update(stmt)
