package nyanda

trait MockPreparedStatement[F[_]] extends PreparedStatement[F] {
  def executeQuery(): F[nyanda.ResultSet[F]] = ???
  def executeUpdate(): F[Int] = ???
  def setInt(parameterIndex: Int, x: Int): F[Unit] = ???
  def setLong(parameterIndex: Int, x: Long): F[Unit] = ???
  def setObject(parameterIndex: Int, x: Object): F[Unit] = ???
  def setShort(parameterIndex: Int, x: Short): F[Unit] = ???
  def setString(parameterIndex: Int, x: String): F[Unit] = ???
  def setTimestamp(parameterIndex: Int, x: java.sql.Timestamp): F[Unit] = ???
}
