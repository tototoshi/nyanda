package sjdbc.core

import java.sql.ResultSet
import scala.collection.mutable.ListBuffer

class ResultSetOps(rs: ResultSet):

  def option[A](parser: ResultSetParser[A]): Option[A] =
    if (rs.next()) {
      val result = parser.parse(rs)
      if (rs.next()) {
        throw new TooManyRowException("More than single record are found")
      } else {
        Some(result)
      }
    } else {
      None
    }

  def seq[A](parser: ResultSetParser[A]): Seq[A] =
    val buffer: ListBuffer[A] = ListBuffer.empty[A]
    while (rs.next()) {
      buffer += parser.parse(rs)
    }
    buffer.toSeq
