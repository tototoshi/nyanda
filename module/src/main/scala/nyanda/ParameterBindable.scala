package nyanda

import java.time.Instant
import java.util.Date
import java.time.ZonedDateTime
import java.time.LocalDateTime
import java.time.ZoneId

trait ParameterBindable[F[_], -A]:
  self =>

  def binder(value: A): ParameterBinder[F]

  def from[B](f: B => A): ParameterBindable[F, B] = new ParameterBindable[F, B] {
    def binder(value: B): ParameterBinder[F] = self.binder(f(value))
  }

object ParameterBindable:

  def apply[F[_], A](fn: (PreparedStatement[F], Int, A) => F[Unit]) = new ParameterBindable[F, A] {
    def binder(value: A): ParameterBinder[F] = ParameterBinder[F](fn(_, _, value))
  }

trait ParameterBindableInstances[F[_]]:

  implicit def idBind: ParameterBindable[F, ParameterBinder[F]] = new ParameterBindable[F, ParameterBinder[F]] {
    def binder(value: ParameterBinder[F]): ParameterBinder[F] = value
  }

  implicit def intBind: ParameterBindable[F, Int] = ParameterBindable[F, Int] { (statement, index, value) =>
    statement.setInt(index, value)
  }

  implicit def longBind: ParameterBindable[F, Long] = ParameterBindable[F, Long] { (statement, index, value) =>
    statement.setLong(index, value)
  }

  implicit def shortBind: ParameterBindable[F, Short] = ParameterBindable[F, Short] { (statement, index, value) =>
    statement.setShort(index, value)
  }

  implicit def stringBind: ParameterBindable[F, String] = ParameterBindable[F, String] { (statement, index, value) =>
    statement.setString(index, value)
  }

  implicit def javaSqlTimestampBind: ParameterBindable[F, java.sql.Timestamp] =
    ParameterBindable[F, java.sql.Timestamp] { (statement, index, value) =>
      statement.setTimestamp(index, value)
    }

  implicit def javaUtilDateBind(implicit b: ParameterBindable[F, java.sql.Timestamp]): ParameterBindable[F, Date] =
    b.from(d => new java.sql.Timestamp(d.getTime))

  implicit def javaTimeInstantBind(implicit
      b: ParameterBindable[F, java.sql.Timestamp]
  ): ParameterBindable[F, Instant] =
    b.from(java.sql.Timestamp.from)

  implicit def zonedDateTimeBind(implicit
      b: ParameterBindable[F, java.time.Instant]
  ): ParameterBindable[F, ZonedDateTime] =
    b.from(zdt => zdt.toInstant)

  implicit def localDateTimeBind(implicit
      b: ParameterBindable[F, java.time.ZonedDateTime]
  ): ParameterBindable[F, LocalDateTime] = b.from(ldt => ZonedDateTime.of(ldt, ZoneId.systemDefault))

  implicit def optionBind[T](implicit b: ParameterBindable[F, T]): ParameterBindable[F, Option[T]] =
    b.from(o => o.orNull.asInstanceOf[T])

  implicit def noneBind: ParameterBindable[F, None.type] =
    ParameterBindable[F, None.type] { (statement, index, value) => statement.setObject(index, null) }
