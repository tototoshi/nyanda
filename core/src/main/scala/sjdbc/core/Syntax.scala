package sjdbc.core

import java.sql.Connection
import java.sql.ResultSet

trait Syntax:

  extension (sc: StringContext)
    def sql(args: ParameterBinder*): SQL = {
      val strings = sc.parts.iterator
      val expressions = args.iterator
      SQL(
        statement = strings.mkString("?"),
        params = expressions.toSeq
      )
    }

  implicit def toParameterBinder[A](value: A)(implicit f: ParameterBinderFactory[A]): ParameterBinder =
    f.binder(value)

  extension (connection: Connection) def query[A](stmt: SQL): ResultSet = new ConnectionOps(connection).query(stmt)
  extension (connection: Connection) def update[A](stmt: SQL): Int = new ConnectionOps(connection).update(stmt)

  extension (rs: ResultSet)
    def option[A](parser: ResultSetParser[A]): Option[A] = new ResultSetOps(rs).option(parser)
    def seq[A](parser: ResultSetParser[A]): Seq[A] = new ResultSetOps(rs).seq(parser)
    def string(name: String) = rs.getString(name)
    def int(name: String) = rs.getInt(name)
    def long(name: String) = rs.getLong(name)
