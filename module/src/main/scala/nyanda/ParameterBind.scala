package nyanda

trait ParameterBind[F[_], -T]:
  def bind(statement: PreparedStatement[F], index: Int, value: T): F[Unit]

object ParameterBind:
  def from[F[_], A, B](f: B => A)(using a: ParameterBind[F, A]): ParameterBind[F, B] =
    new ParameterBind[F, B] {
      def bind(statement: PreparedStatement[F], index: Int, value: B): F[Unit] =
        a.bind(statement, index, f(value))
    }

trait ParameterBindInstances[F[_]]:

  given ParameterBind[F, Int] with
    def bind(statement: PreparedStatement[F], index: Int, value: Int): F[Unit] =
      statement.setInt(index, value)

  given ParameterBind[F, Short] with
    def bind(statement: PreparedStatement[F], index: Int, value: Short): F[Unit] =
      statement.setShort(index, value)

  given ParameterBind[F, Long] with
    def bind(statement: PreparedStatement[F], index: Int, value: Long): F[Unit] =
      statement.setLong(index, value)

  given ParameterBind[F, String] with
    def bind(statement: PreparedStatement[F], index: Int, value: String): F[Unit] =
      statement.setString(index, value)

  given ParameterBind[F, java.sql.Timestamp] with
    def bind(statement: PreparedStatement[F], index: Int, value: java.sql.Timestamp): F[Unit] =
      statement.setTimestamp(index, value)

  given ParameterBind[F, java.util.Date] = ParameterBind.from(d => new java.sql.Timestamp(d.getTime))

  given ParameterBind[F, java.time.Instant] = ParameterBind.from(java.sql.Timestamp.from)

  given ParameterBind[F, java.time.ZonedDateTime] = ParameterBind.from(_.toInstant)

  given ParameterBind[F, java.time.LocalDateTime] =
    ParameterBind.from(java.time.ZonedDateTime.of(_, java.time.ZoneId.systemDefault))

  given ParameterBind[F, Null] with
    def bind(statement: PreparedStatement[F], index: Int, value: Null): F[Unit] =
      statement.setObject(index, null)

  given ParameterBind[F, None.type] = ParameterBind.from(_ => null)

  given [T](using s: ParameterBind[F, T], n: ParameterBind[F, Null]): ParameterBind[F, Option[T]] with
    def bind(statement: PreparedStatement[F], index: Int, value: Option[T]): F[Unit] =
      value match {
        case Some(v) => s.bind(statement, index, v)
        case None => n.bind(statement, index, null)
      }
