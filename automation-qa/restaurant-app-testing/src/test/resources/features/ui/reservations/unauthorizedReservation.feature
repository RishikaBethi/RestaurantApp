Feature: Handle Customer reservations
  Background:
    Given the user enters into the application
    And the user clicks on "Book a Table" button

  Scenario Outline: Verify the user cannot reserve a table without logging in
    When the user selects the "<location>" "<date>" "<timeSlot>" "<guests>" details
    And the user clicks on "Find a Table" button
    And the user clicks on a timeslot
    Then the user enters into the sign in page

    Examples:
      | location | date       | timeSlot | guests |
      | LOC003   | 21-05-2027 | 10:30    | 1      |
