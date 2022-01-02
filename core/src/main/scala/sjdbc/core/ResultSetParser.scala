package sjdbc.core

import java.sql.ResultSet

trait ResultSetParser[T]:
  def parse(rs: ResultSet): T

object ResultSetParser:
  def apply[T](f: ResultSet => T): ResultSetParser[T] =
    new ResultSetParser[T] { def parse(rs: ResultSet): T = f(rs) }
