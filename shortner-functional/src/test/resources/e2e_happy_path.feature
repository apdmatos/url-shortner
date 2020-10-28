Feature: E2E happy path scenarios

  @HappyPath
  Scenario: Happy path scenario to get redirected to a website
    Given a rest service
    When I call the save url endpoint with the url http://www.google.com
    Then an ok response is returned with a shortUrl
    And I do a get on the shortUrl returned
    And I get redirected to the url http://www.google.com