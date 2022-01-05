package nyanda

import java.sql.ResultSet
import scala.collection.mutable.ListBuffer

trait ResultSetReader[T]:
  self =>

  private def iterator(rs: ResultSet): Iterator[ResultSet] = Iterator.unfold(rs) { rs =>
    if (rs.next()) Some((rs, rs)) else None
  }

  def read(rs: ResultSet): T

  def option: ResultSetReader[Option[T]] =
    new ResultSetReader[Option[T]] {
      def read(rs: ResultSet): Option[T] =
        val it = iterator(rs)
        it.take(1).toSeq.headOption.map(self.read)
    }

  def seq: ResultSetReader[Seq[T]] =
    new ResultSetReader[Seq[T]] {
      def read(rs: ResultSet): Seq[T] =
        val it = iterator(rs)
        it.map(self.read).toSeq
    }

object ResultSetReader:
  def apply[T](f: ResultSet => T): ResultSetReader[T] =
    new ResultSetReader[T] { def read(rs: ResultSet): T = f(rs) }
