package support

import munit.FunSuite
import scala.concurrent.duration._

class ConfigSpec extends FunSuite {

  test("Config should have default values") {
    assertEquals(Config.baseUrl, "https://example.com")
    assertEquals(Config.profile, "smoke")
    assertEquals(Config.users, 10)
    assertEquals(Config.rampDuration, 60.seconds)
  }

  test("Config should sum weights to 1.0") {
    val totalWeight = Config.searchWeight + Config.browseWeight + Config.transactionWeight
    assertEqualsDouble(totalWeight, 1.0, 0.001)
  }

  test("Config should calculate success rate from error rate") {
    assertEquals(Config.sloSuccessRatePct, 100.0 - Config.sloErrorRatePct)
  }
}
