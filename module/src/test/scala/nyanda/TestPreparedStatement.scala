package nyanda

trait MockPreparedStatement[F[_]] extends PreparedStatement[F] {
  def executeQuery(): F[nyanda.ResultSet[F]] = ???
  def executeUpdate(): F[Int] = ???
  def setArray(parameterIndex: Int, x: java.sql.Array): F[Unit] = ???
  def setBoolean(parameterIndex: Int, x: Boolean): F[Unit] = ???
  def setByte(parameterIndex: Int, x: Byte): F[Unit] = ???
  def setBytes(parameterIndex: Int, x: Array[Byte]): F[Unit] = ???
  def setDate(parameterIndex: Int, x: java.sql.Date): F[Unit] = ???
  def setDouble(parameterIndex: Int, x: Double): F[Unit] = ???
  def setFloat(parameterIndex: Int, x: Float): F[Unit] = ???
  def setInt(parameterIndex: Int, x: Int): F[Unit] = ???
  def setLong(parameterIndex: Int, x: Long): F[Unit] = ???
  def setObject(parameterIndex: Int, x: Object): F[Unit] = ???
  def setShort(parameterIndex: Int, x: Short): F[Unit] = ???
  def setString(parameterIndex: Int, x: String): F[Unit] = ???
  def setTimestamp(parameterIndex: Int, x: java.sql.Timestamp): F[Unit] = ???
  def setTime(parameterIndex: Int, x: java.sql.Time): F[Unit] = ???
}
