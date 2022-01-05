package nyanda

import java.sql.ResultSet
import scala.collection.mutable.ListBuffer

class ResultSetOps(rs: ResultSet):

  private def it: Iterator[ResultSet] = Iterator.unfold(rs) { rs => if (rs.next()) Some((rs, rs)) else None }

  def seq[A](reader: ResultSetReader[A]): ResultSetReader[Seq[A]] =
    new ResultSetReader[Seq[A]] {
      def read(rs: ResultSet): Seq[A] = it.map(reader.read).toSeq
    }
