package nyanda

trait SQLSyntax[F[_]]:

  extension (sc: StringContext)
    def sql(args: ParameterBinder[F]*): SQL[F] =
      val strings = sc.parts.iterator
      val expressions = args.iterator
      SQL[F](
        statement = strings.mkString("?"),
        params = expressions.toSeq
      )
