package support

import io.gatling.core.Predef._
import io.gatling.core.structure.PopulationBuilder
import io.gatling.core.structure.ScenarioBuilder

import scala.concurrent.duration._

/** Injection profile factories.
  *
  * Each factory produces an injection sequence shaped by [[Config]] for a single scenario.
  * Simulations combine them per scenario.
  */
object LoadProfiles {

  /** Round to at least 1, weighted from a total user count. */
  def share(weight: Double, total: Int): Int =
    math.max(1, math.round(weight * total).toInt)

  def smoke(scn: ScenarioBuilder, weight: Double): PopulationBuilder =
    scn.inject(atOnceUsers(math.max(1, math.round(weight * 4).toInt)))

  def rampUp(scn: ScenarioBuilder, weight: Double): PopulationBuilder =
    scn.inject(rampUsers(share(weight, Config.users)).during(Config.rampDuration))

  def stress(scn: ScenarioBuilder, weight: Double): PopulationBuilder =
    scn.inject(
      rampUsersPerSec(1).to(share(weight, Config.peakUsers)).during(Config.rampDuration)
    )

  def spike(scn: ScenarioBuilder, weight: Double): PopulationBuilder =
    scn.inject(
      nothingFor(10.seconds),
      atOnceUsers(share(weight, Config.spikeUsers))
    )

  def soak(scn: ScenarioBuilder, weight: Double): PopulationBuilder =
    scn.inject(
      constantUsersPerSec(share(weight, Config.users).toDouble).during(Config.soakDuration)
    )

  /** Build a population for `scn` based on [[Config.profile]]. */
  def forProfile(scn: ScenarioBuilder, weight: Double): PopulationBuilder =
    Config.profile match {
      case "smoke"  => smoke(scn, weight)
      case "rampup" => rampUp(scn, weight)
      case "stress" => stress(scn, weight)
      case "spike"  => spike(scn, weight)
      case "soak"   => soak(scn, weight)
      case other =>
        println(s"[gatling-demo] Unknown LOAD_PROFILE='$other', falling back to smoke")
        smoke(scn, weight)
    }
}
