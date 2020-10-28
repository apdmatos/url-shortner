package simulations

import scala.concurrent.duration._
import scenarios.{GetUrlScenario, SaveUrlScenario}
import io.gatling.core.Predef._
import io.gatling.http.Predef._

class NftTests extends Simulation {
  val group: String = "NftTest"
  val url: String = "http://localhost:8080"
  var durationMinutes: Int = 2

  val httpConf = http
    .baseUrl(url)
    .shareConnections
    .disableWarmUp

  setUp(
    GetUrlScenario.createScenario(durationMinutes, httpConf),
    SaveUrlScenario.createScenario(durationMinutes, httpConf)
  )
    .assertions(GetUrlScenario.getAssertions())
    .assertions(SaveUrlScenario.getAssertions())
    .maxDuration(durationMinutes minutes)

}
