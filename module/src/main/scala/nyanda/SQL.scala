package nyanda

case class SQL(statement: String, params: Seq[ParameterBinder])
