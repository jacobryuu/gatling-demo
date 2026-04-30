package simulations

import io.gatling.core.Predef._
import support._

/**
 * Checkout / transaction-heavy workload. Useful for verifying write-path
 * performance, idempotency handling, DB write contention, and downstream
 * payment/inventory dependencies.
 *
 * Run:
 *   sbt "Gatling/testOnly simulations.CheckoutFlowSimulation"
 */
class CheckoutFlowSimulation extends Simulation {

  before {
    println(s"[gatling-demo] CheckoutFlowSimulation starting with: ${Config.toString}")
  }

  setUp(
    LoadProfiles.forProfile(Scenarios.transaction, 1.0)
  ).protocols(HttpProtocols.default)
   .assertions(Slo.all: _*)
}
