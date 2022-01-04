package nyanda

import java.sql.ResultSet
import scala.collection.mutable.ListBuffer

class ResultSetOps(rs: ResultSet):

  def option[A](reader: ResultSetReader[A]): ResultSetReader[Option[A]] =
    new ResultSetReader[Option[A]] {
      def read(rs: ResultSet): Option[A] =
        if (rs.next()) {
          val result = reader.read(rs)
          if (rs.next()) {
            throw new TooManyRowException("More than single record are found")
          } else {
            Some(result)
          }
        } else {
          None
        }
    }

  def seq[A](reader: ResultSetReader[A]): ResultSetReader[Seq[A]] =
    new ResultSetReader[Seq[A]] {
      def read(rs: ResultSet): Seq[A] =
        val buffer: ListBuffer[A] = ListBuffer.empty[A]
        while (rs.next()) {
          buffer += reader.read(rs)
        }
        buffer.toSeq
    }
