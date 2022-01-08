package nyanda

case class SQL[F[_]](statement: String, params: Seq[ParameterBinder[F]])
