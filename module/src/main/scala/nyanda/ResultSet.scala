package nyanda

import cats._
import cats.implicits._
import cats.effect.Sync

trait ResultSet[F[_]]:
  def next(): F[Boolean]
  def getInt(columnLabel: String): F[Int]
  def getLong(columnLabel: String): F[Long]
  def getShort(columnLabel: String): F[Short]
  def getString(columnLabel: String): F[String]
  def getTimestamp(columnLabel: String): F[java.sql.Timestamp]
  def getDate(columnLabel: String): F[java.sql.Date]

object ResultSet:

  def apply[F[_]: Sync](rs: java.sql.ResultSet): ResultSet[F] =
    new ResultSet[F] {
      def next(): F[Boolean] = Sync[F].blocking(rs.next())
      def getInt(columnLabel: String): F[Int] = Sync[F].blocking(rs.getInt(columnLabel))
      def getLong(columnLabel: String): F[Long] = Sync[F].blocking(rs.getLong(columnLabel))
      def getShort(columnLabel: String): F[Short] = Sync[F].blocking(rs.getShort(columnLabel))
      def getString(columnLabel: String): F[String] = Sync[F].blocking(rs.getString(columnLabel))
      def getTimestamp(columnLabel: String): F[java.sql.Timestamp] = Sync[F].blocking(rs.getTimestamp(columnLabel))
      def getDate(columnLabel: String): F[java.sql.Date] = Sync[F].blocking(rs.getDate(columnLabel))
    }
