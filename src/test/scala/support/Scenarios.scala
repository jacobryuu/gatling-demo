package support

import io.gatling.core.Predef._
import io.gatling.core.feeder.FeederBuilder
import io.gatling.core.structure.ScenarioBuilder

import scala.concurrent.duration._

/** Shared scenario builders used by multiple Simulations. */
object Scenarios {

  // ---- Feeders ---------------------------------------------------------------

  val users: FeederBuilder       = csv(Config.usersCsv).circular
  val searchTerms: FeederBuilder = csv("feeders/search_terms.csv").random
  val txData: FeederBuilder      = csv("feeders/transaction_data.csv").random

  // ---- Scenarios -------------------------------------------------------------

  val search: ScenarioBuilder =
    scenario("Search")
      .feed(users)
      .exec(Journeys.login)
      .feed(searchTerms)
      .exec(Journeys.search)
      .pause(1.second, 3.seconds)

  val browse: ScenarioBuilder =
    scenario("Browse")
      .feed(users)
      .exec(Journeys.login)
      .feed(searchTerms)
      .exec(Journeys.search)
      .pause(500.millis, 2.seconds)
      .exec(Journeys.browseDetail)
      .pause(1.second, 3.seconds)

  val transaction: ScenarioBuilder =
    scenario("Transaction")
      .feed(users)
      .exec(Journeys.login)
      .feed(searchTerms)
      .exec(Journeys.search)
      .pause(500.millis, 1.second)
      .feed(txData)
      .exec(Journeys.transaction)
}
