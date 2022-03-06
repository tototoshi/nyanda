package nyanda

import cats.*
import cats.implicits.*

trait Dsl[F[_]] extends SQLSyntax[F]:
  val DB: DatabaseOps[F]
  val RS: ResultSetOps[F]

object Dsl:

  def apply[F[_]](using ev: Dsl[F]): Dsl[F] = ev

  trait Sync[F[_]: cats.effect.Sync] extends Dsl[F]:
    val DB: DatabaseOps[F] = DatabaseOps[F]
    val RS: ResultSetOps[F] = ResultSetOps[F]

  trait IO extends Sync[cats.effect.IO]

  object IO extends IO

  given [F[_]: cats.effect.Sync](using dbOps: DatabaseOps[F], rsOps: ResultSetOps[F]): Dsl[F] =
    new Sync[F] {}
