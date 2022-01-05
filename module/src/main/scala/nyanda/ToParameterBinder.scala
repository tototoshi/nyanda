package nyanda

import java.sql.PreparedStatement
import java.time.Instant
import java.util.Date
import java.time.ZonedDateTime
import java.time.LocalDateTime
import java.time.ZoneId

trait ToParameterBinder[-A]:
  self =>

  def binder(value: A): ParameterBinder

  def from[B](f: B => A): ToParameterBinder[B] = new ToParameterBinder[B] {
    def binder(value: B): ParameterBinder = self.binder(f(value))
  }

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

  implicit def javaSqlTimestampBind: ToParameterBinder[java.sql.Timestamp] = ToParameterBinder {
    (statement, index, value) =>
      statement.setTimestamp(index, value)
  }

  implicit def javaUtilDateBind(implicit b: ToParameterBinder[java.sql.Timestamp]): ToParameterBinder[Date] =
    b.from(d => new java.sql.Timestamp(d.getTime))

  implicit def javaTimeInstantBind(implicit b: ToParameterBinder[java.sql.Timestamp]): ToParameterBinder[Instant] =
    b.from(java.sql.Timestamp.from)

  implicit def zonedDateTimeBind(implicit b: ToParameterBinder[java.time.Instant]): ToParameterBinder[ZonedDateTime] =
    b.from(zdt => zdt.toInstant)

  implicit def localDateTimeBind(implicit
      b: ToParameterBinder[java.time.ZonedDateTime]
  ): ToParameterBinder[LocalDateTime] = b.from(ldt => ZonedDateTime.of(ldt, ZoneId.systemDefault))

  implicit def optionBind[T](implicit b: ToParameterBinder[T]): ToParameterBinder[Option[T]] =
    b.from(o => o.orNull.asInstanceOf[T])

  implicit def noneBind: ToParameterBinder[None.type] =
    ToParameterBinder { (statement, index, value) => statement.setObject(index, null) }
