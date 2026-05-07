package support

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder

/** Reusable user-behavior chains.
  *
  * Endpoints are placeholders ("/login", "/items", "/items/{id}", "/transactions"). Replace
  * paths/payloads to match the target API. Sessions persist Cookies automatically; tokens are
  * stored in the Gatling session via checks and applied per-request as headers when AUTH_MODE=token
  * (EL evaluated at request time).
  */
object Journeys {

  private def withAuth(req: HttpRequestBuilder): HttpRequestBuilder =
    if (Config.authMode == "token") req.header("Authorization", "Bearer #{authToken}")
    else req

  // ---- Auth ------------------------------------------------------------------

  val login: ChainBuilder = {
    val base = http("Login")
      .post("/login")
      .body(StringBody("""{"username":"#{username}","password":"#{password}"}"""))
      .asJson
      .check(status.in(200, 201, 204))

    val withTokenCheck =
      if (Config.authMode == "token") base.check(jsonPath("$.token").saveAs("authToken"))
      else base

    exec(withTokenCheck)
  }

  // ---- Read paths ------------------------------------------------------------

  val search: ChainBuilder =
    exec(
      withAuth(
        http("Search items")
          .get("/items")
          .queryParam("q", "#{searchTerm}")
      )
        .check(status.in(200, 204))
        .check(jsonPath("$.items[0].id").optional.saveAs("itemId"))
    )

  val browseDetail: ChainBuilder =
    doIfOrElse(session => session.contains("itemId")) {
      exec(
        withAuth(http("Item detail").get("/items/#{itemId}"))
          .check(status.in(200, 204))
      )
    } {
      exec(
        withAuth(http("Item detail (fallback)").get("/items/1"))
          .check(status.in(200, 204))
      )
    }

  // ---- Transaction ----------------------------------------------------------

  val transaction: ChainBuilder =
    exec(
      withAuth(
        http("Create transaction")
          .post("/transactions")
          .body(
            StringBody(
              """{"itemId":"#{itemId}","quantity":#{quantity},"idempotencyKey":"#{idempotencyKey}"}"""
            )
          )
          .asJson
      )
        .check(status.in(200, 201, 202))
    )
}
