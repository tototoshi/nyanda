package nyanda

import cats._
import cats.implicits._
import cats.effect.Sync

trait ResultSet[F[_]]:
  def next(): F[Boolean]
  def getInt(columnLabel: String): F[Int]
  def getString(columnLabel: String): F[String]

object ResultSet:

  def apply[F[_]: Sync](rs: java.sql.ResultSet): ResultSet[F] =
    new ResultSet[F] {
      def next(): F[Boolean] = Sync[F].blocking(rs.next())
      def getInt(columnLabel: String): F[Int] = Sync[F].blocking(rs.getInt(columnLabel))
      def getString(columnLabel: String): F[String] = Sync[F].blocking(rs.getString(columnLabel))

    }
