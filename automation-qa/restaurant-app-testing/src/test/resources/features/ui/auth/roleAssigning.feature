Feature: Verify the role assignment for customer and waiter

  Background:
    Given the user enters into the application

  Scenario Outline: Verify the role of the customer and waiter
    Given the user enters into the sign in page
    When the user enters the "<email>" and "<password>"
    And the user clicks on "sign in" button
    And the user clicks on user logo button
    Then the user will be assigned the "<role>" role

    Examples:
      | email               | password     | role                      |
      | sushmag@example.com | Password123! | Sushma Reddy (Customer)   |
      | sushma@waiter.com   | Password123! | Sushma Gantagari (Waiter) |

