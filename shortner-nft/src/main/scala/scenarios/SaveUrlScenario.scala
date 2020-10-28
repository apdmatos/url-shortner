package scenarios

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.core.structure.PopulationBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import io.gatling.http.request.builder.HttpRequestBuilder

import scala.collection.mutable.ListBuffer

object SaveUrlScenario {

  def getRequest(): HttpRequestBuilder = {
    http("SaveUrl")
      .post("/")
      .headers(Map(
        "Content-Type" -> "application/json",
        "Accept" -> "application/json",
      ))
      .body(StringBody(s"""{"url": "http://www.google.com"}"""))
      .check(status.is(200))
  }

  def createScenario(duration: Int, httpConf: HttpProtocolBuilder) : PopulationBuilder = {

    scenario("SaveUrl")
      .exec(getRequest())
      .inject(
        constantUsersPerSec(20) during (duration minutes)
      )
      .protocols(httpConf)
  }

  def getAssertions(): List[Assertion] = {
    val assertionList = new ListBuffer[Assertion]()
    assertionList += details("SaveUrl").failedRequests.count.lte(1)
    assertionList += details("SaveUrl").responseTime.percentile4.lt(500)

    assertionList.toList
  }
}