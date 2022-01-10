package nyanda

import cats.data.Kleisli
import cats.effect.IO

type Query[F[_], A] = Kleisli[F, Connection[F], A]
type QueryIO[A] = Query[IO, A]
