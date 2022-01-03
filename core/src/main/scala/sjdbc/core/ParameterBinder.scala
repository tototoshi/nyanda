package sjdbc.core

import java.sql.PreparedStatement

trait ParameterBinder:
  def bind(statement: PreparedStatement, index: Int): Unit

object ParameterBinder:
  def apply(fn: (PreparedStatement, Int) => Unit): ParameterBinder = new ParameterBinder {
    def bind(statement: PreparedStatement, index: Int): Unit = fn(statement, index)
  }

trait ParameterBinderFactory[A]:
  def binder(value: A): ParameterBinder

object ParameterBinderFactory:

  def apply[A](fn: (PreparedStatement, Int, A) => Unit) = new ParameterBinderFactory[A] {
    def binder(value: A): ParameterBinder = ParameterBinder(fn(_, _, value))
  }

  implicit def idBind: ParameterBinderFactory[ParameterBinder] = new ParameterBinderFactory[ParameterBinder] {
    def binder(value: ParameterBinder): ParameterBinder = value
  }

  implicit def intBind: ParameterBinderFactory[Int] = ParameterBinderFactory { (statement, index, value) =>
    statement.setInt(index, value)
  }

  implicit def stringBind: ParameterBinderFactory[String] = ParameterBinderFactory { (statement, index, value) =>
    statement.setString(index, value)
  }
