package sjdbc.core

case class SQL(statement: String, params: Seq[ParameterBinder])
