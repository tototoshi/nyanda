package nyanda

class TestResultSet[F[_]] extends ResultSet[F] {
  def getArray(columnLabel: String): F[java.sql.Array] = ???
  def getBigDecimal(columnLabel: String): F[BigDecimal] = ???
  def getBoolean(columnLabel: String): F[Boolean] = ???
  def getByte(columnLabel: String): F[Byte] = ???
  def getBytes(columnLabel: String): F[Array[Byte]] = ???
  def getDate(columnLabel: String): F[java.sql.Date] = ???
  def getDouble(columnLabel: String): F[Double] = ???
  def getFloat(columnLabel: String): F[Float] = ???
  def getInt(columnLabel: String): F[Int] = ???
  def getLong(columnLabel: String): F[Long] = ???
  def getShort(columnLabel: String): F[Short] = ???
  def getString(columnLabel: String): F[String] = ???
  def getTime(columnLabel: String): F[java.sql.Time] = ???
  def getTimestamp(columnLabel: String): F[java.sql.Timestamp] = ???
  def next(): F[Boolean] = ???
}
