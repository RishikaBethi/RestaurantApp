Feature: Enable reservation for authorized users

  Background:
    Given the base_uri of the application
    Given user sends a sign in request with the following data
      | email               | password     |
      | sushmag@example.com | Password123! |
    When the user sends the post request to "/auth/sign-in" with the request payload
    Then the status code should be 201
    And the token is stored

  Scenario: Verify display of reservations for an authorized user
    When the user sends the authorized get request to "/reservations" request payload
    Then the status code should be 200
    And the response should validate the "ValidateReservations" schema

  Scenario: Verify display of error message when unauthorized user
    When the user sends the get request to "/reservations" request payload
    Then the status code should be 401
    And the response should contain failed error "Unauthorized: Email not found in token." message

  Scenario Outline: Verify authorized users can delete their reservations
    When the user sends the authorized delete request to "/reservations/<id>" request payload
    Then the status code should be <status>

    Examples:
      | id                                   | status |
      | 4f34fc7c-70c3-49e4-80fb-ebec47e87b77 | 204    |
      | 1234                                 | 404    |