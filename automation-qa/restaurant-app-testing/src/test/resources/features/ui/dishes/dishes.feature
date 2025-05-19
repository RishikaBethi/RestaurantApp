Feature: Verify whether the dishes are displayed to the user

  Background:
    Given the user enters into the application

  @smoke
  Scenario: Verify the visible of static menu on main page
    Then the user should be able to see the available "static" dishes

  @regression
  Scenario: Verify the unauthorized users cannot view the menu
    When the user clicks on "View Menu" button
    Then the page will display the "Please login to browse or view the menu." dishes message

  @regression
  Scenario: Verify the authorized users should able to browse menu
    Given the user enters into the sign in page
    And the user enters the "sushmag@example.com" and "Password123!"
    And the user clicks on "sign in" button
    When the user clicks on "View Menu" button
    Then the user should be able to see the available "dynamic" dishes

  @regression
  Scenario Outline: Verify the filtering of dishes
    Given the user enters into the sign in page
    And the user enters the "sushmag@example.com" and "Password123!"
    And the user clicks on "sign in" button
    When the user clicks on "View Menu" button
    And the user selects the "<order type>" from dropdown
    Then verify whether the price is sorted "<order type>" order

    Examples:
    |order type  |
    | Price Low to High           |
    |   Price High to Low         |

