package nyanda

import java.time.Instant
import java.util.Date
import java.time.ZonedDateTime
import java.time.LocalDateTime
import java.time.ZoneId

trait ToParameterBinder[F[_], -A]:
  self =>

  def binder(value: A): ParameterBinder[F]

  def from[B](f: B => A): ToParameterBinder[F, B] = new ToParameterBinder[F, B] {
    def binder(value: B): ParameterBinder[F] = self.binder(f(value))
  }

object ToParameterBinder:

  def apply[F[_], A](fn: (PreparedStatement[F], Int, A) => F[Unit]) = new ToParameterBinder[F, A] {
    def binder(value: A): ParameterBinder[F] = ParameterBinder[F](fn(_, _, value))
  }

trait ToParameterBinderInstances[F[_]]:

  implicit def idBind: ToParameterBinder[F, ParameterBinder[F]] = new ToParameterBinder[F, ParameterBinder[F]] {
    def binder(value: ParameterBinder[F]): ParameterBinder[F] = value
  }

  implicit def intBind: ToParameterBinder[F, Int] = ToParameterBinder[F, Int] { (statement, index, value) =>
    statement.setInt(index, value)
  }

  implicit def longBind: ToParameterBinder[F, Long] = ToParameterBinder[F, Long] { (statement, index, value) =>
    statement.setLong(index, value)
  }

  implicit def shortBind: ToParameterBinder[F, Short] = ToParameterBinder[F, Short] { (statement, index, value) =>
    statement.setShort(index, value)
  }

  implicit def stringBind: ToParameterBinder[F, String] = ToParameterBinder[F, String] { (statement, index, value) =>
    statement.setString(index, value)
  }

  implicit def javaSqlTimestampBind: ToParameterBinder[F, java.sql.Timestamp] =
    ToParameterBinder[F, java.sql.Timestamp] { (statement, index, value) =>
      statement.setTimestamp(index, value)
    }

  implicit def javaUtilDateBind(implicit b: ToParameterBinder[F, java.sql.Timestamp]): ToParameterBinder[F, Date] =
    b.from(d => new java.sql.Timestamp(d.getTime))

  implicit def javaTimeInstantBind(implicit
      b: ToParameterBinder[F, java.sql.Timestamp]
  ): ToParameterBinder[F, Instant] =
    b.from(java.sql.Timestamp.from)

  implicit def zonedDateTimeBind(implicit
      b: ToParameterBinder[F, java.time.Instant]
  ): ToParameterBinder[F, ZonedDateTime] =
    b.from(zdt => zdt.toInstant)

  implicit def localDateTimeBind(implicit
      b: ToParameterBinder[F, java.time.ZonedDateTime]
  ): ToParameterBinder[F, LocalDateTime] = b.from(ldt => ZonedDateTime.of(ldt, ZoneId.systemDefault))

  implicit def optionBind[T](implicit b: ToParameterBinder[F, T]): ToParameterBinder[F, Option[T]] =
    b.from(o => o.orNull.asInstanceOf[T])

  implicit def noneBind: ToParameterBinder[F, None.type] =
    ToParameterBinder[F, None.type] { (statement, index, value) => statement.setObject(index, null) }
