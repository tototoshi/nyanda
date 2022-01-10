package nyanda

import cats.data.Kleisli
import cats.effect.IO

type QueryF[F[_], A] = Kleisli[F, Connection[F], A]
type Query[A] = QueryF[IO, A]
