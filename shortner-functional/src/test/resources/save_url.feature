Feature: Tests to save url APi exploring all edge cases

  @SAVE_URL
  Scenario: SaveUrl with invalid payload
    Given a rest service
    When I call the save url endpoint with invalid payload
    Then I get status code 400

  @SAVE_URL
  Scenario: SaveUrl with invalid content-type header
    Given a rest service
    When I call the save url endpoint with invalid ContentType
    Then I get status code 415

  @SAVE_URL
  Scenario: SaveUrl with invalid accepts header
    Given a rest service
    When I call the save url endpoint with invalid Accepts
    Then I get status code 406

  @SAVE_URL
  Scenario: SaveUrl with no payload
    Given a rest service
    When I call the save url endpoint with no payload
    Then I get status code 400

  @SAVE_URL
  Scenario: SaveUrl with invalid url
    Given a rest service
    When I call the save url endpoint with the url INVALID_URL
    Then I get status code 400