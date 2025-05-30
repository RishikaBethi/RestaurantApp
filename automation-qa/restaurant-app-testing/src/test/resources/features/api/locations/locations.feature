Feature: Display the locations for authorized and unauthorized users

  Background:
    Given the base_uri of the application

  @smoke
  Scenario Outline: Verify the successful user sign in and assign the token
    Given user sends a sign in request with the following data
      | email   | password   |
      | <email> | <password> |
    When the user sends the post request to "/auth/sign-in" with the request payload
    Then the status code should be 201

    Examples:
      | email                | password     | role     |
      | sushmag@example.com  | Password123! | Customer |

    @regression
  Scenario Outline: Verify the successful user sign in and assign the token
    Given user sends a sign in request with the following data
      | email   | password   |
      | <email> | <password> |
    When the user sends the post request to "/auth/sign-in" with the request payload
    Then the status code should be 201
    And the response should contain the "<role>" assigned
    And the response should validate the "ValidateSignIn" schema
    And the response should contain the token id of the user based on "<role>"

    Examples:
      | email                | password     | role     |
      | sushmag@example.com  | Password123! | Customer |
      | vardhini@example.com | Password123! | Waiter   |

  @regression
  Scenario: Verify the display of locations to authorized user
    When the user sends the authorized get request to "/locations" request payload
    Then the status code should be 200
    And the response should validate the "ValidateLocations" schema

  @regression
  Scenario: Verify the display of locations to unauthorized users
    When the user sends the get request to "/locations" request payload
    Then the status code should be 200
    And the response should validate the "ValidateLocations" schema

  @regression
  Scenario Outline: Verify the display of special dishes for valid location id
    When the user sends the get request to "/locations/<locationId>/speciality-dishes" request payload
    Then the status code should be <status>
    And the response should validate the "ValidateSpecialDishes" schema

    Examples:
      | locationId | status |
      | LOC001     | 200    |

  @regression
  Scenario Outline: Verify the handling of special dishes for invalid location id
    When the user sends the get request to "/locations/<locationId>/speciality-dishes" request payload
    Then the status code should be 404
    And the response should contain failed error "Location not found" message

    Examples:
      | locationId |
      | Le456rfght |

  @regression
  Scenario Outline: Verify the display of special dishes for valid location id
    When the user sends the authorized get request to "/locations/<locationId>/speciality-dishes" request payload
    Then the status code should be <status>
    And the response should validate the "ValidateSpecialDishes" schema

    Examples:
      | locationId | status |
      | LOC001     | 200    |

  @regression
  Scenario Outline: Verify the filteration of feedbacks
    When the user sends the get request to "/locations/<locationId>/feedbacks?<filter>" request payload
    Then the status code should be 200

    Examples:
      | locationId | filter                                 | ValidateFeedbacks                  |
      | LOC001     | type=CUISINE_EXPERIENCE                | ValidateCuisineFeedbacks           |
      | LOC001     | type=SERVICE                           | ValidateServiceFeedbacks           |
      | LOC001     | type=SERVICE&type=CUISINE_EXPERIENCE   | ValidateCuisineAndServiceFeedbacks |
      | LOC001     | type=CUISINE_EXPERIENCE&sort=date_desc | ValidateCuisineWithDate            |
      | LOC001     | type=SERVICE&page=1&size=5             | ValidateFeedbackPagination         |
      | LOC001     | type=SERVICE&sort=date_asc             | ValidateServiceFeedbackWithDate    |


  @regression
  Scenario Outline: Verify the filteration of feedbacks for authorized users
    When the user sends the authorized get request to "/locations/<locationId>/feedbacks?<filter>" request payload
    Then the status code should be 200
    #And the response should validate the "ValidateFeedbacks" schema

    Examples:
      | locationId | filter                                 |
      | LOC001     | type=CUISINE_EXPERIENCE                |
      | LOC001     | type=SERVICE                           |
      | LOC001     | type=SERVICE&type=CUISINE_EXPERIENCE   |
      | LOC001     | type=CUISINE_EXPERIENCE&sort=date,desc |
      | LOC001     | type=SERVICE&page=1&size=5             |
      | LOC001     | type=SERVICE&?sort=date_asc            |

  @regression
  Scenario Outline: Verify the filteration of feedbacks for invalid location id
    When the user sends the authorized get request to "/locations/<locationId>/feedbacks?<filter>" request payload
    Then the status code should be 400
    And the response should contain failed error "Invalid location ID" message

    Examples:
      | locationId | filter                                 |
      | LOC00451   | type=CUISINE_EXPERIENCE                |
      | LOC03301   | type=SERVICE                           |
      | LOCef001   | type=SERVICE&type=CUISINE_EXPERIENCE   |
      | LOC003v1   | type=CUISINE_EXPERIENCE&sort=date,desc |
      | LO3rC001   | type=SERVICE&page=1&size=5             |
      | LO34f001   | type=SERVICE&?sort=date_asc            |

  @regression
  Scenario: Verify the return of available locations
    When the user sends the authorized get request to "/locations/select-options" request payload
    Then the status code should be 200
    And the response should validate the "ValidateTableOptions" schema
