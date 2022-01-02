package sjdbc.core

import java.sql.PreparedStatement

trait ParameterBinder {
  def bind(statement: PreparedStatement, index: Int): Unit
}
