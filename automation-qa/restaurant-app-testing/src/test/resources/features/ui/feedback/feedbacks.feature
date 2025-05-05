Feature: Verify Feedbacks functionality

  Background:
    Given the user enters into the application
    Given the user enters into the sign in page
    When the user enters the "rg@email.com" and "Password123!"
    And the user clicks on "sign in" button
    And the user clicks on "Reservations" button

  Scenario: User gives feedback with all valid details
    When the user clicks on "Update Feedback" button
    And the user gives 4 stars
    And adds a comment
    And the user clicks on "Culinary Experience" button
    And the user gives 5 stars
    And adds a comment
    And the user clicks the Update Feedback button
    Then the page will display the feedback created message

    Scenario: User gives feedback without any comments
      When the user clicks on "Update Feedback" button
      And the user gives 4 stars
      And gives no comment
      And the user clicks on "Culinary Experience" button
      And the user gives 4 stars
      And gives no comment
      And the user clicks the Update Feedback button
      Then the page will display the feedback created message