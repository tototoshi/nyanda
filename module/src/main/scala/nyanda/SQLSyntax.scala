package nyanda

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

  given [A](using ParameterBind[F, A]): Conversion[A, ParameterBinder[F]] with
    def apply(value: A): ParameterBinder[F] = ParameterBinder[F, A](value)
