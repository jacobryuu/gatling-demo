package simulations

import io.gatling.core.Predef._
import support._

/**
 * Search-only workload. Useful for stressing read paths or search infrastructure
 * (e.g. Elasticsearch, full-text indexes).
 *
 * Run:
 *   sbt "Gatling/testOnly simulations.SearchOnlySimulation"
 */
class SearchOnlySimulation extends Simulation {

  before {
    println(s"[gatling-demo] SearchOnlySimulation starting with: ${Config.toString}")
  }

  setUp(
    LoadProfiles.forProfile(Scenarios.search, 1.0)
  ).protocols(HttpProtocols.default)
   .assertions(Slo.all: _*)
}
