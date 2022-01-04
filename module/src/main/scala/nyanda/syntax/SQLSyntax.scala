package nyanda.syntax

import java.sql.Connection
import javax.sql.DataSource
import java.sql.ResultSet
import nyanda._

trait SQLSyntax:

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
