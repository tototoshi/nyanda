package nyanda

import java.sql.ResultSet
import scala.collection.mutable.ListBuffer

trait ResultSetReader[T]:
  self =>

  def read(rs: ResultSet): T

  def option: ResultSetReader[Option[T]] =
    new ResultSetReader[Option[T]] {
      def read(rs: ResultSet): Option[T] =
        if (rs.next()) {
          val result = self.read(rs)
          if (rs.next()) {
            throw new TooManyRowException("More than single record are found")
          } else {
            Some(result)
          }
        } else {
          None
        }
    }

  def seq: ResultSetReader[Seq[T]] =
    new ResultSetReader[Seq[T]] {
      def read(rs: ResultSet): Seq[T] =
        val buffer: ListBuffer[T] = ListBuffer.empty[T]
        while (rs.next()) {
          buffer += self.read(rs)
        }
        buffer.toSeq
    }

object ResultSetReader:
  def apply[T](f: ResultSet => T): ResultSetReader[T] =
    new ResultSetReader[T] { def read(rs: ResultSet): T = f(rs) }
