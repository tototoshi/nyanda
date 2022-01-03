package sjdbc.core

import java.sql.PreparedStatement

trait ParameterBinder:
  def bind(statement: PreparedStatement, index: Int): Unit

object ParameterBinder:
  def apply(fn: (PreparedStatement, Int) => Unit): ParameterBinder = new ParameterBinder {
    def bind(statement: PreparedStatement, index: Int): Unit = fn(statement, index)
  }

trait ParameterBinderFactory[-A]:
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

  implicit def longBind: ParameterBinderFactory[Long] = ParameterBinderFactory { (statement, index, value) =>
    statement.setLong(index, value)
  }

  implicit def shortBind: ParameterBinderFactory[Short] = ParameterBinderFactory { (statement, index, value) =>
    statement.setShort(index, value)
  }

  implicit def stringBind: ParameterBinderFactory[String] = ParameterBinderFactory { (statement, index, value) =>
    statement.setString(index, value)
  }

  implicit def optionBind[T](implicit b: ParameterBinderFactory[T]): ParameterBinderFactory[Option[T]] =
    new ParameterBinderFactory[Option[T]] {
      def binder(value: Option[T]): ParameterBinder =
        value match {
          case Some(v) => b.binder(v)
          case None => b.binder(null.asInstanceOf[T])
        }
    }

  implicit def noneBind: ParameterBinderFactory[None.type] =
    ParameterBinderFactory { (statement, index, value) => statement.setObject(index, null) }
