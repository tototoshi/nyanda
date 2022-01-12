package nyanda

import cats._
import cats.implicits._
import cats.effect.Sync

trait PreparedStatement[F[_]]:
  def executeQuery(): F[ResultSet[F]]
  def executeUpdate(): F[Int]
  def setArray(parameterIndex: Int, x: java.sql.Array): F[Unit]
  def setBigDecimal(parameterIndex: Int, x: BigDecimal): F[Unit]
  def setBoolean(parameterIndex: Int, x: Boolean): F[Unit]
  def setByte(parameterIndex: Int, x: Byte): F[Unit]
  def setBytes(parameterIndex: Int, x: Array[Byte]): F[Unit]
  def setDate(parameterIndex: Int, x: java.sql.Date): F[Unit]
  def setDouble(parameterIndex: Int, x: Double): F[Unit]
  def setFloat(parameterIndex: Int, x: Float): F[Unit]
  def setInt(parameterIndex: Int, x: Int): F[Unit]
  def setLong(parameterIndex: Int, x: Long): F[Unit]
  def setObject(parameterIndex: Int, x: java.lang.Object): F[Unit]
  def setShort(parameterIndex: Int, x: Short): F[Unit]
  def setString(parameterIndex: Int, x: String): F[Unit]
  def setTime(parameterIndex: Int, x: java.sql.Time): F[Unit]
  def setTimestamp(parameterIndex: Int, x: java.sql.Timestamp): F[Unit]

object PreparedStatement:

  def apply[F[_]: Sync](stmt: java.sql.PreparedStatement): PreparedStatement[F] =
    new PreparedStatement[F]:
      def executeQuery(): F[ResultSet[F]] = Sync[F].blocking(stmt.executeQuery()).map(rs => ResultSet[F](rs))
      def executeUpdate(): F[Int] = Sync[F].blocking(stmt.executeUpdate())
      def setArray(parameterIndex: Int, x: java.sql.Array): F[Unit] =
        Sync[F].blocking(stmt.setArray(parameterIndex, x))
      def setBigDecimal(parameterIndex: Int, x: BigDecimal): F[Unit] =
        Sync[F].blocking(stmt.setBigDecimal(parameterIndex, x.bigDecimal))
      def setBoolean(parameterIndex: Int, x: Boolean): F[Unit] =
        Sync[F].blocking(stmt.setBoolean(parameterIndex, x))
      def setByte(parameterIndex: Int, x: Byte): F[Unit] =
        Sync[F].blocking(stmt.setByte(parameterIndex, x))
      def setBytes(parameterIndex: Int, x: Array[Byte]): F[Unit] =
        Sync[F].blocking(stmt.setBytes(parameterIndex, x))
      def setDate(parameterIndex: Int, x: java.sql.Date): F[Unit] =
        Sync[F].blocking(stmt.setDate(parameterIndex, x))
      def setDouble(parameterIndex: Int, x: Double): F[Unit] =
        Sync[F].blocking(stmt.setDouble(parameterIndex, x))
      def setFloat(parameterIndex: Int, x: Float): F[Unit] =
        Sync[F].blocking(stmt.setFloat(parameterIndex, x))
      def setInt(parameterIndex: Int, x: Int): F[Unit] =
        Sync[F].blocking(stmt.setInt(parameterIndex, x))
      def setLong(parameterIndex: Int, x: Long): F[Unit] =
        Sync[F].blocking(stmt.setLong(parameterIndex, x))
      def setObject(parameterIndex: Int, x: java.lang.Object): F[Unit] =
        Sync[F].blocking(stmt.setObject(parameterIndex, x))
      def setShort(parameterIndex: Int, x: Short): F[Unit] =
        Sync[F].blocking(stmt.setShort(parameterIndex, x))
      def setString(parameterIndex: Int, x: String): F[Unit] =
        Sync[F].blocking(stmt.setString(parameterIndex, x))
      def setTime(parameterIndex: Int, x: java.sql.Time): F[Unit] =
        Sync[F].blocking(stmt.setTime(parameterIndex, x))
      def setTimestamp(parameterIndex: Int, x: java.sql.Timestamp): F[Unit] =
        Sync[F].blocking(stmt.setTimestamp(parameterIndex, x))
