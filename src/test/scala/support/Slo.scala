package support

import io.gatling.core.Predef._
import io.gatling.commons.stats.assertion.Assertion

/**
 * Standard SLO assertions builder.
 *
 * Each Simulation calls `.assertions(Slo.all: _*)` after `setUp(...)`.
 */
object Slo {

  def all: Seq[Assertion] = Seq(
    global.responseTime.percentile(95.0).lt(Config.sloP95Ms),
    global.responseTime.percentile(99.0).lt(Config.sloP99Ms),
    global.successfulRequests.percent.gt(Config.sloSuccessRatePct),
    forAll.failedRequests.percent.lt(Config.sloErrorRatePct)
  )
}
