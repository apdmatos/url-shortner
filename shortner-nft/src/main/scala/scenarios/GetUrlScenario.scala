package scenarios

import java.util.UUID

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.core.structure.PopulationBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import io.gatling.http.request.builder.HttpRequestBuilder

import scala.collection.mutable.ListBuffer

object GetUrlScenario {

  def getRequest(shortCode: String): HttpRequestBuilder = {
    http("GetUrl")
      .get(s"/$shortCode")
      .check(status.is(404))
  }

  def createScenario(duration: Int, httpConf: HttpProtocolBuilder) : PopulationBuilder = {

    val feeder = Iterator.continually(Map("shortCode" -> {
      UUID.randomUUID().toString
    }))

    scenario("GetUrl")
      .exec(feed(feeder))
      .exec(getRequest("${shortCode}"))
      .inject(
        constantUsersPerSec(20) during (duration minutes)
      )
      .protocols(httpConf)
  }

  def getAssertions(): List[Assertion] = {
    val assertionList = new ListBuffer[Assertion]()
    assertionList += details("GetUrl").failedRequests.count.lte(1)
    assertionList += details("GetUrl").responseTime.percentile4.lt(10)

    assertionList.toList
  }
}