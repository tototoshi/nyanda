package nyanda.syntax

import java.sql.ResultSet

trait ResultSetSyntax:
  extension (rs: ResultSet)
    def string(name: String) = rs.getString(name)
    def int(name: String) = rs.getInt(name)
    def long(name: String) = rs.getLong(name)
