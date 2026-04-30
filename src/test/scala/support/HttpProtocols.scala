package support

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

/** Shared HTTP protocol configuration used by all simulations. */
object HttpProtocols {

  val default: HttpProtocolBuilder =
    http
      .baseUrl(Config.baseUrl)
      .acceptHeader("application/json, text/html;q=0.9, */*;q=0.8")
      .acceptLanguageHeader("ja,en;q=0.8")
      .acceptEncodingHeader("gzip, deflate")
      .userAgentHeader("gatling-demo/1.0 (+performance-test)")
      .contentTypeHeader("application/json")
      .disableWarmUp
      .shareConnections
      .disableCaching
}
