package nyanda

import cats.*
import cats.implicits.*

trait Dsl[F[_]] extends SQLSyntax[F]:
  val DB: DatabaseOps[F]
  val RS: ResultSetOps[F]

object Dsl:

  def apply[F[_]](using ev: Dsl[F]): Dsl[F] = ev

  given [F[_]](using dbOps: DatabaseOps[F], rsOps: ResultSetOps[F]): Dsl[F] =
    new Dsl[F]:
      val DB: DatabaseOps[F] = dbOps
      val RS: ResultSetOps[F] = rsOps
