package nyanda

import cats.effect.Sync
import nyanda.syntax.SQLSyntax

trait Dsl[F[_]: Sync]
    extends ResultSetGetInstances[F]
    with ResultSetReadInstances[F]
    with ParameterBindInstances[F]
    with DataSourceSyntax
    with SQLSyntax[F] {
  object DB extends DatabaseOps[F]
  object RS extends ResultSetOps[F]
}

object Dsl:
  def apply[F[_]: Sync]: Dsl[F] = new Dsl[F] {}
