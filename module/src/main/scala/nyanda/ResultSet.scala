package nyanda

import cats._
import cats.implicits._
import cats.effect.Sync

trait ResultSet[F[_]]:
  def next(): F[Boolean]
  def getArray(columnLabel: String): F[java.sql.Array]
  def getBoolean(columnLabel: String): F[Boolean]
  def getByte(columnLabel: String): F[Byte]
  def getBytes(columnLabel: String): F[Array[Byte]]
  def getInt(columnLabel: String): F[Int]
  def getLong(columnLabel: String): F[Long]
  def getShort(columnLabel: String): F[Short]
  def getFloat(columnLabel: String): F[Float]
  def getDouble(columnLabel: String): F[Double]
  def getString(columnLabel: String): F[String]
  def getTimestamp(columnLabel: String): F[java.sql.Timestamp]
  def getDate(columnLabel: String): F[java.sql.Date]
  def getTime(columnLabel: String): F[java.sql.Time]

object ResultSet:

  def apply[F[_]: Sync](rs: java.sql.ResultSet): ResultSet[F] =
    new ResultSet[F]:
      def next(): F[Boolean] = Sync[F].blocking(rs.next())
      def getArray(columnLabel: String): F[java.sql.Array] = Sync[F].blocking(rs.getArray(columnLabel))
      def getBoolean(columnLabel: String): F[Boolean] = Sync[F].blocking(rs.getBoolean(columnLabel))
      def getByte(columnLabel: String): F[Byte] = Sync[F].blocking(rs.getByte(columnLabel))
      def getBytes(columnLabel: String): F[Array[Byte]] = Sync[F].blocking(rs.getBytes(columnLabel))
      def getInt(columnLabel: String): F[Int] = Sync[F].blocking(rs.getInt(columnLabel))
      def getLong(columnLabel: String): F[Long] = Sync[F].blocking(rs.getLong(columnLabel))
      def getShort(columnLabel: String): F[Short] = Sync[F].blocking(rs.getShort(columnLabel))
      def getFloat(columnLabel: String): F[Float] = Sync[F].blocking(rs.getFloat(columnLabel))
      def getDouble(columnLabel: String): F[Double] = Sync[F].blocking(rs.getDouble(columnLabel))
      def getString(columnLabel: String): F[String] = Sync[F].blocking(rs.getString(columnLabel))
      def getTimestamp(columnLabel: String): F[java.sql.Timestamp] = Sync[F].blocking(rs.getTimestamp(columnLabel))
      def getDate(columnLabel: String): F[java.sql.Date] = Sync[F].blocking(rs.getDate(columnLabel))
      def getTime(columnLabel: String): F[java.sql.Time] = Sync[F].blocking(rs.getTime(columnLabel))
