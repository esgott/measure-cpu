package com.github.esgott.mcpu.server

import cats.effect.{ExitCode, IO}
import cats.syntax.all._
import com.monovore.decline.Opts
import com.monovore.decline.effect.CommandIOApp

object Server extends CommandIOApp(name = "measure-cpu-server", header = "Measure CPU Server") {

  case class Config(
      host: String,
      port: Int,
      bfNumEntries: Int,
      bfFpProb: Double,
      bfDefaultWidth: Int
  )

  private val hostOpts: Opts[String] =
    Opts.option[String](long = "host", short = "h", metavar = "host", help = "Host address")

  private val portOpts: Opts[Int] =
    Opts.option[Int](long = "port", short = "p", metavar = "port", help = "Port")

  private val bfNumEntriesOpts: Opts[Int] =
    Opts
      .option[Int](
        long = "bloom-filter-num-entries",
        short = "n",
        metavar = "num-entries",
        help = "Bloom filter number of entries"
      )
      .withDefault(1000)

  private val bfFpProdOpts: Opts[Double] =
    Opts
      .option[Double](
        long = "bloom-filter-false-positive-probability",
        short = "f",
        metavar = "probability",
        help = "Bloom filter false positive probability"
      )
      .withDefault(0.001)

  private val bfDefaultWidthOpts: Opts[Int] =
    Opts
      .option[Int](
        long = "bloom-filter-default-width",
        short = "w",
        metavar = "width",
        help = "If the bloom filter is not able to calculate width automatically, use this"
      )
      .withDefault(128)

  private val opts: Opts[Config] =
    (hostOpts, portOpts, bfNumEntriesOpts, bfFpProdOpts, bfDefaultWidthOpts).mapN(Config.apply)

  override def main: Opts[IO[ExitCode]] = opts.map { config =>
    ServerComponents(config).use { components =>
      import components._

      for {
        _ <- components.messageBus.receive
               .map(_._2)
               .through(Deduplicator.pipe[IO])
               .through(EventStore.pipe[IO])
               .compile
               .drain
      } yield ExitCode.Success
    }
  }

}
