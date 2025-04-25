Feature: Verify the visibility of locations to the user

  Background:
    Given the user enters into the application

  Scenario: Verify the visibility of locations
    When the user scrolls to the locations view
    Then the user should find the locations