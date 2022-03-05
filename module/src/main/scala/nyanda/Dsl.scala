package nyanda

import cats._
import cats.implicits._
import cats.data.Kleisli
import cats.effect.Sync
import nyanda.DatabaseOps

trait Dsl[F[_]]
    extends SQLSyntax[F]
    with ParameterBindInstances
    with ResultSetConsumeInstances
    with ResultSetGetInstances:
  val DB: DatabaseOps[F]
  val RS: ResultSetOps[F]

object Dsl:

  def apply[F[_]](using ev: Dsl[F]): Dsl[F] = ev

  given [F[_]: Sync](using dbOps: DatabaseOps[F], rsOps: ResultSetOps[F]): Dsl[F] =
    new Dsl[F]:
      val DB: DatabaseOps[F] = dbOps
      val RS: ResultSetOps[F] = rsOps
