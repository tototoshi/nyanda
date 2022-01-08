package nyanda

trait ParameterBinder[F[_]]:
  def bind(statement: PreparedStatement[F], index: Int): F[Unit]

object ParameterBinder:
  def apply[F[_]](fn: (PreparedStatement[F], Int) => F[Unit]): ParameterBinder[F] = new ParameterBinder[F] {
    def bind(statement: PreparedStatement[F], index: Int): F[Unit] = fn(statement, index)
  }
