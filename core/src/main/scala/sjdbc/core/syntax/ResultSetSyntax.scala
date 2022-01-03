package sjdbc.core.syntax

import sjdbc.core._
import java.sql.ResultSet

trait ResultSetSyntax:
  extension (rs: ResultSet)
    def option[A](parser: ResultSetParser[A]): Option[A] = new ResultSetOps(rs).option(parser)
    def seq[A](parser: ResultSetParser[A]): Seq[A] = new ResultSetOps(rs).seq(parser)
    def string(name: String) = rs.getString(name)
    def int(name: String) = rs.getInt(name)
    def long(name: String) = rs.getLong(name)
