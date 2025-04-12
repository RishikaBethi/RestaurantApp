Feature: Validate the user sign in and role assigning
  Background:
    Given the base_uri of the application

  Scenario Outline: Verify the successful user sign in
    Given user sends a sign in request with the following data
    | email    |password    |
    |<email>   |<password>  |
    When the user sends the post request to "/auth/sign-in" with the request payload
    Then the status code should be 201
    And the response should contain the "<role>" assigned
    And the response should validate the "ValidateSignIn" schema

    Examples:
    |      email                     | password                  |   role          |
    |sushma@example.com              |Y2!kjqKHX                  | Customer        |
    |sophia.jones41@example.com      |Y2!kjqKHX                  | Waiter          |

  Scenario Outline: Verify sign in with missing and empty fields
    Given user sends a sign in  request with the following data
    |email              |   password|
    |<email>            |<password> |
    When the user sends the post request to "/auth/sign-in" with the request payload
    Then the status code should be 400
    And the response should contain failed error "<error>" message

    Examples:

    |email                     | password                |  error                            |
    |                          |abc12!@3s                |Invalid Email                      |
    |jhon_smith@example.com    |                         |Password is required               |
    |       ""                 |abc12!@3s                |Invalid Email                      |
    #|jhon_smith@example.com    | ""                      |Password is required               |

  Scenario Outline: Sign in with unregistered email or incorrect password
    Given user sends a sign in  request with the following data
    |email        | password |
    |<email>      |<password>|
    When the user sends the post request to "/auth/sign-in" with the request payload
    Then the status code should be 401
    And the response should contain failed error "Incorrect email or password. Try again or create an account." message

    Examples:
    |email                  | password     |
    |nonexistent@example.com|John@123      |
    |sushma@example.com     |Y!kjqKHX      |


  Scenario Outline: Block the user after 3 invalid sign in attempts
    Given user sends a signup request with the following data
    |email          | password   |
    |<email>        |<password>  |
    When the user sends the post request to "/auth/sign-in" with the request payload
    Then the status code should be <status>
    And the response should contain failed error or message for <status> based on "<message_or_error>" message

    Examples:
    |email             | password   | status  | message_or_error                                          |
    |sushmag@example.com        |Y!kjqKHX    |401      |Incorrect email or password. Try again or create an account.|
    |sushmag@example.com      |Y!kjqKHX    |401      |Incorrect email or password. Try again or create an account.|
    | sushmag@example.comjohn@e.com        |Y!kjqKHX    |401      |Incorrect email or password. Try again or create an account.|
    |sushmag@example.com        |Y!kjqKHX    |403      |Your account is temporarily locked due to multiple failed login attempts. Please try again later.|




