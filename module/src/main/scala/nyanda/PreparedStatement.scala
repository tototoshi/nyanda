package nyanda

import cats._
import cats.implicits._
import cats.effect.Sync

trait PreparedStatement[F[_]]:
  def executeQuery(): F[ResultSet[F]]
  def executeUpdate(): F[Int]
  def setInt(parameterIndex: Int, x: Int): F[Unit]
  def setLong(parameterIndex: Int, x: Long): F[Unit]
  def setObject(parameterIndex: Int, x: java.lang.Object): F[Unit]
  def setShort(parameterIndex: Int, x: Short): F[Unit]
  def setString(parameterIndex: Int, x: String): F[Unit]
  def setTimestamp(parameterIndex: Int, x: java.sql.Timestamp): F[Unit]

object PreparedStatement:

  def apply[F[_]: Sync](stmt: java.sql.PreparedStatement): PreparedStatement[F] =
    new PreparedStatement[F] {
      def executeQuery(): F[ResultSet[F]] = Sync[F].blocking(stmt.executeQuery()).map(rs => ResultSet[F](rs))
      def executeUpdate(): F[Int] = Sync[F].blocking(stmt.executeUpdate())

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
      def setTimestamp(parameterIndex: Int, x: java.sql.Timestamp): F[Unit] =
        Sync[F].blocking(stmt.setTimestamp(parameterIndex, x))
    }
