Feature: Tests to get url APi exploring all edge cases

  @GET_URL
  Scenario: Get non existing short code
    Given a rest service
    When I do a get on the shortCode 237e9877-e79b-12d4-a765-321741963000
    Then I get status code 404

  @GET_URL
  Scenario: Get with non valid format
    Given a rest service
    When I do a get on the shortCode foo
    Then I get status code 404