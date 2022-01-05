package nyanda

import java.sql.PreparedStatement

trait ParameterBinder:
  def bind(statement: PreparedStatement, index: Int): Unit

object ParameterBinder:
  def apply(fn: (PreparedStatement, Int) => Unit): ParameterBinder = new ParameterBinder {
    def bind(statement: PreparedStatement, index: Int): Unit = fn(statement, index)
  }
