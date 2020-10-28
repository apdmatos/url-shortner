package pt.smartthought.url;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import pt.smartthought.url.config.ServiceConfig;
import pt.smartthought.url.dto.PostUrl;

import static io.restassured.RestAssured.given;

@Slf4j
public class Stepdefs {

    private static String DEFAULT_URL = "http://www.bing.com";

    private final ServiceConfig conf;

    private RequestSpecification request;
    private Response response;
    private String shortUrl;

    @Autowired
    public Stepdefs(ServiceConfig conf) {
        this.conf = conf;
    }

   @Before
   public void init() {
        response = null;
        shortUrl = null;
   }

    @Given("^a rest service$")
    public void aRestService() {
        request = given().baseUri(conf.getUrl());
    }

    @When("^I call the save url endpoint with the url (.*)$")
    public void i_call_the_save_endpoint(String url) {
        response = request
                .header(new Header("Content-Type", "application/json"))
                .header(new Header("Accepts", "application/json"))
                .body(new PostUrl(url))
                .post("/");
    }

    @When("^I call the save url endpoint with invalid payload$")
    public void i_call_the_save_endpoint_with_invalid_payload() {
        response = request
                .header(new Header("Content-Type", "application/json"))
                .header(new Header("Accepts", "application/json"))
                .body("{'foo': 'www.google.com'}")
                .post("/");
    }

    @When("^I call the save url endpoint with no payload$")
    public void i_call_the_save_endpoint_with_no_payload() {
        response = request
                .header(new Header("Content-Type", "application/json"))
                .header(new Header("Accepts", "application/json"))
                .body("")
                .post("/");
    }

    @When("^I call the save url endpoint with invalid ContentType$")
    public void i_call_the_save_endpoint_with_invalid_content_type() {
        response = request
                .header(new Header("Content-Type", "application/xml"))
                .header(new Header("Accepts", "application/json"))
                .body("{'url':'http://www.bing.com'}")
                .post("/");
    }

    @When("I call the save url endpoint with invalid Accepts")
    public void i_call_the_save_endpoint_with_invalid_accepts() {
        response = request.given()
                .header(new Header("Content-Type", "application/json"))
                .header(new Header("Accept", "application/xml"))
                .body(new PostUrl(DEFAULT_URL))
                .post("/");
    }

    @Then("^an ok response is returned with a shortUrl$")
    public void an_ok_response_is_returned_with_shortUrl() {
        response.then().statusCode(200);
        shortUrl = response.jsonPath().getString("url");

        Assert.assertNotNull(shortUrl);
        Assert.assertNotEquals("", shortUrl);
    }

    @Then("^I do a get on the shortUrl returned$")
    public void i_call_the_get_endpoint() {
        response = given()
                    .redirects().follow(false)
                .get(shortUrl);
    }

    @Then("^I do a get on the shortCode (.*)$")
    public void i_call_the_get_endpoint_with_shortcode(String shortCode) {
        response = request
                .when()
                .redirects().follow(false)
                .get("/" + shortUrl);
    }

    @Then("^I get redirected to the url (.*)$")
    public void i_get_redirected_to(String redirectUrl) {
        response.then().statusCode(302);
        String locationHeader = response.header("Location");

        Assert.assertEquals(redirectUrl, locationHeader);
    }

    @Then("I get status code (.*)$")
    public void i_get_the_status_code(String statusCode) {
        int status = Integer.parseInt(statusCode);
        response.then().statusCode(status);
    }
}
