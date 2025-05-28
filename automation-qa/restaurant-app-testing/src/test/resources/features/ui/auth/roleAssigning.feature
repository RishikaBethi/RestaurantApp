Feature: Verify the role assignment for customer and waiter

  Background:
    Given the user enters into the application

  @smoke
  Scenario Outline: Verify the role of the customer and waiter
    Given the user enters into the sign in page
    When the user enters the "<email>" and "<password>"
    And the user clicks on "sign in" button
    And the user clicks on user logo button
    Then the user will be assigned the "<role>" role

    Examples:
      | email              | password     | role               |
      | abcdef@example.com | Abcdef123!   | abc def (Customer) |
      | sushma@waiter.com  | Password123! | Sushma W (Waiter)  |