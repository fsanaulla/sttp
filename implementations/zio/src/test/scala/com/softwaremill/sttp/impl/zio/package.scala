package com.softwaremill.sttp.impl

import com.softwaremill.sttp.testing.ConvertToFuture
import _root_.zio._

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

package object zio {

  val convertZioIoToFuture: ConvertToFuture[IO[Throwable, ?]] = new ConvertToFuture[IO[Throwable, ?]] {
    override def toFuture[T](value: IO[Throwable, T]): Future[T] = {
      val p = Promise[T]()
      val runtime = new DefaultRuntime {}

      runtime.unsafeRunSync(value) match {
        case Exit.Failure(c) =>
          p.complete(
            Failure(
              c.failures.headOption.orElse(c.defects.headOption).getOrElse(new RuntimeException(s"Unknown cause: $c"))
            )
          )
        case Exit.Success(v) => p.complete(Success(v))
      }

      p.future
    }
  }
}
