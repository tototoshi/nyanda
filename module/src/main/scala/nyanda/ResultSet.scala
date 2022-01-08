package nyanda

import cats._
import cats.implicits._
import cats.effect.Sync

trait ResultSet[F[_]]:
  def next(): F[Boolean]
  def getInt(columnLabel: String): F[Int]
  def getString(columnLabel: String): F[String]

object ResultSet:

  def apply[F[_]: Sync](resultSet: => java.sql.ResultSet): ResultSet[F] =
    val rs = Sync[F].delay(resultSet)

    new ResultSet[F] {
      def next(): F[Boolean] = rs.map(_.next())
      def getInt(columnLabel: String): F[Int] = rs.map(_.getInt(columnLabel))
      def getString(columnLabel: String): F[String] = rs.map(_.getString(columnLabel))

    }
