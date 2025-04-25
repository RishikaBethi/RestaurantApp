Feature: Verify the sign in of the user

  Background:
    Given the user enters into the application
    Given the user enters into the sign in page

  Scenario Outline: Verify successful login with valid data
    When the user enters the "<email>" and "<password>"
    And the user clicks on sign in button
    Then the user will be redirected to the "<main>" page

    Examples:
      | email               | password     | main                                                                   |
      | sushmag@example.com | Password123! | http://team-7-frontend-bucket.s3-website-ap-southeast-2.amazonaws.com/ |


  Scenario Outline: Verify unregistered user cannot sign in into the application
    When the user enters the "<email>" and "<password>"
    And the user clicks on sign in button
    Then the page will display the error "<error>" message

    Examples:
      | email              | password  | error                                                        |
      | sindhu@example.com | susha@W21 | Incorrect email or password. Try again or create an account. |

  Scenario Outline: Verify missing credentials
    When the user enters the "<email>" and "<password>"
    And the user clicks on sign in button
    Then the page will display the missing fields error "<error>" message

    Examples:
      | email              | password  | error                 |
      |                    | susha@W21 | Email is required.    |
      | sindhu@example.com |           | Password is required. |


  Scenario Outline: Verify whether the account is blocked after 3 invalid attempts
    When the user enters the "<email>" and "<password>"
    And the user clicks on sign in button
    Then the page will display the error "<error>" message

    Examples:
      | email                      | password  | error                                                                                             |
      | sindhujareddyg@example.com | susha@W21 | Incorrect email or password. Try again or create an account.                                      |
      | sindhujareddyg@example.com | susha@W21 | Incorrect email or password. Try again or create an account.                                      |
      | sindhujareddyg@example.com | susha@W21 | Incorrect email or password. Try again or create an account.                                      |
      | sindhujareddyg@example.com | susha@W21 | Your account is temporarily locked due to multiple failed login attempts. Please try again later. |