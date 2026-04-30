package support

import scala.concurrent.duration._
import scala.util.Try

/**
 * Centralized configuration loaded from environment variables / system properties.
 * All performance test parameters can be overridden without modifying source.
 */
object Config {

  // ---- Helpers ---------------------------------------------------------------

  private def env(key: String): Option[String] =
    Option(System.getProperty(key)).orElse(Option(System.getenv(key))).map(_.trim).filter(_.nonEmpty)

  private def envOr(key: String, default: String): String = env(key).getOrElse(default)

  private def envInt(key: String, default: Int): Int =
    env(key).flatMap(v => Try(v.toInt).toOption).getOrElse(default)

  private def envDouble(key: String, default: Double): Double =
    env(key).flatMap(v => Try(v.toDouble).toOption).getOrElse(default)

  private def envDuration(key: String, default: FiniteDuration): FiniteDuration =
    env(key).flatMap(v => Try(v.toLong).toOption).map(_.seconds).getOrElse(default)

  // ---- Target ----------------------------------------------------------------

  val baseUrl: String = envOr("BASE_URL", "https://example.com")

  // ---- Load profile ----------------------------------------------------------

  /** Profile selector: smoke | rampup | stress | spike | soak */
  val profile: String = envOr("LOAD_PROFILE", "smoke").toLowerCase

  val users: Int                   = envInt("USERS", 10)
  val rampDuration: FiniteDuration = envDuration("RAMP_DURATION_SECONDS", 60.seconds)
  val holdDuration: FiniteDuration = envDuration("HOLD_DURATION_SECONDS", 60.seconds)
  val peakUsers: Int               = envInt("PEAK_USERS", 1000)
  val spikeUsers: Int              = envInt("SPIKE_USERS", 500)
  val soakDuration: FiniteDuration = envDuration("SOAK_DURATION_SECONDS", 1800.seconds)

  // ---- Scenario weights (must sum to ~1.0) -----------------------------------

  val searchWeight: Double      = envDouble("WEIGHT_SEARCH", 0.7)
  val browseWeight: Double      = envDouble("WEIGHT_BROWSE", 0.2)
  val transactionWeight: Double = envDouble("WEIGHT_TRANSACTION", 0.1)

  // ---- SLO thresholds --------------------------------------------------------

  val sloP95Ms: Int             = envInt("SLO_P95_MS", 1000)
  val sloP99Ms: Int             = envInt("SLO_P99_MS", 2000)
  val sloErrorRatePct: Double   = envDouble("SLO_ERROR_RATE_PCT", 1.0)
  val sloSuccessRatePct: Double = 100.0 - sloErrorRatePct

  // ---- Authentication --------------------------------------------------------

  /** Auth strategy: cookie | token | none */
  val authMode: String = envOr("AUTH_MODE", "cookie").toLowerCase

  // ---- Feeders ---------------------------------------------------------------

  /**
   * Path to the users CSV resource. Override via `USERS_CSV` to inject
   * CI-secret-backed credentials at runtime instead of the dummy file in VCS.
   */
  val usersCsv: String = envOr("USERS_CSV", "feeders/users.csv")

  override def toString: String =
    s"""Config(
       |  baseUrl=$baseUrl,
       |  profile=$profile,
       |  users=$users, peakUsers=$peakUsers, spikeUsers=$spikeUsers,
       |  rampDuration=$rampDuration, holdDuration=$holdDuration, soakDuration=$soakDuration,
       |  weights(search=$searchWeight, browse=$browseWeight, transaction=$transactionWeight),
       |  slo(p95=${sloP95Ms}ms, p99=${sloP99Ms}ms, errorRate<${sloErrorRatePct}%),
       |  authMode=$authMode, usersCsv=$usersCsv
       |)""".stripMargin
}
