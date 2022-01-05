package nyanda

import java.sql.ResultSet
import scala.collection.mutable.ListBuffer

class ResultSetOps(rs: ResultSet):

  private def it: Iterator[ResultSet] = Iterator.unfold(rs) { rs => if (rs.next()) Some((rs, rs)) else None }

  def option[A](reader: ResultSetReader[A]): ResultSetReader[Option[A]] =
    new ResultSetReader[Option[A]] {
      def read(rs: ResultSet): Option[A] =
        it.take(2).toList match {
          case Nil => None
          case h :: Nil => Some(reader.read(h))
          case _ => throw new TooManyRowException("More than single record are found")
        }
    }

  def seq[A](reader: ResultSetReader[A]): ResultSetReader[Seq[A]] =
    new ResultSetReader[Seq[A]] {
      def read(rs: ResultSet): Seq[A] = it.map(reader.read).toSeq
    }
