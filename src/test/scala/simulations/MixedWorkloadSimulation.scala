package simulations

import io.gatling.core.Predef._

import support._

/** Mixed workload: search/browse/transaction in weighted parallel.
  *
  * Profile and weights are configured via env vars (LOAD_PROFILE, WEIGHT_*). Default simulation
  * modeling realistic mixed traffic.
  *
  * Run: sbt "Gatling/testOnly simulations.MixedWorkloadSimulation"
  */
class MixedWorkloadSimulation extends Simulation {

  before {
    println(s"[gatling-demo] MixedWorkloadSimulation starting with: ${Config.toString}")
  }

  setUp(
    LoadProfiles.forProfile(Scenarios.search, Config.searchWeight),
    LoadProfiles.forProfile(Scenarios.browse, Config.browseWeight),
    LoadProfiles.forProfile(Scenarios.transaction, Config.transactionWeight)
  ).protocols(HttpProtocols.default)
    .assertions(Slo.all: _*)
}
