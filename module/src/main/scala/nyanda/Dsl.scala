package nyanda

import cats.effect.Sync
import nyanda.syntax.SQLSyntax

trait Dsl[F[_]]
    extends ResultSetGetInstances[F]
    with ResultSetReadInstances[F]
    with ParameterBindInstances[F]
    with DataSourceSyntax
    with SQLSyntax[F]
    with DatabaseOps[F]

object Dsl:
  def apply[F[_]: Sync]: Dsl[F] = new Dsl[F] {}
