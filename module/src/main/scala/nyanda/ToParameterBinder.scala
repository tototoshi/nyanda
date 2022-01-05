package nyanda

import java.sql.PreparedStatement

trait ToParameterBinder[-A]:
  def binder(value: A): ParameterBinder

object ToParameterBinder:

  def apply[A](fn: (PreparedStatement, Int, A) => Unit) = new ToParameterBinder[A] {
    def binder(value: A): ParameterBinder = ParameterBinder(fn(_, _, value))
  }

  implicit def idBind: ToParameterBinder[ParameterBinder] = new ToParameterBinder[ParameterBinder] {
    def binder(value: ParameterBinder): ParameterBinder = value
  }

  implicit def intBind: ToParameterBinder[Int] = ToParameterBinder { (statement, index, value) =>
    statement.setInt(index, value)
  }

  implicit def longBind: ToParameterBinder[Long] = ToParameterBinder { (statement, index, value) =>
    statement.setLong(index, value)
  }

  implicit def shortBind: ToParameterBinder[Short] = ToParameterBinder { (statement, index, value) =>
    statement.setShort(index, value)
  }

  implicit def stringBind: ToParameterBinder[String] = ToParameterBinder { (statement, index, value) =>
    statement.setString(index, value)
  }

  implicit def optionBind[T](implicit b: ToParameterBinder[T]): ToParameterBinder[Option[T]] =
    new ToParameterBinder[Option[T]] {
      def binder(value: Option[T]): ParameterBinder =
        value match {
          case Some(v) => b.binder(v)
          case None => b.binder(null.asInstanceOf[T])
        }
    }

  implicit def noneBind: ToParameterBinder[None.type] =
    ToParameterBinder { (statement, index, value) => statement.setObject(index, null) }
