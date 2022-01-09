package nyanda.syntax

import java.sql.Connection
import javax.sql.DataSource
import java.sql.ResultSet
import nyanda._

trait SQLSyntax[F[_]]:

  extension (sc: StringContext)
    def sql(args: ParameterBinder[F]*): SQL[F] = {
      val strings = sc.parts.iterator
      val expressions = args.iterator
      SQL[F](
        statement = strings.mkString("?"),
        params = expressions.toSeq
      )
    }

  implicit def toParameterBinder[A](value: A)(using ParameterBind[F, A]): ParameterBinder[F] =
    ParameterBinder[F, A](value)
