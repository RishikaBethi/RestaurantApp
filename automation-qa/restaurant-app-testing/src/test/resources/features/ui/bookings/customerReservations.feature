Feature: Handle Customer reservations
  Background:
    Given the user enters into the application
    Given the user enters into the sign in page
    When the user enters the "abcdef@example.com" and "Abcdef123!"
    And the user clicks on "sign in" button
    And the user clicks on "Book a Table" button

  @smoke
  Scenario Outline: Verify that timeslots are visible
    When the user selects the "<location>" "<date>" "<timeSlot>" "<guests>" details
    And the user clicks on "Find a Table" button
    Then the user should be able to see the available timeslots

    Examples:
      | location | date       | timeSlot | guests |
      | LOC003   | 21-05-2027 | 15:45     | 1      |

  @regression
  Scenario Outline: Verify that the users can select the guests count
    When the user selects the "<location>" "<date>" "<timeSlot>" "<guests>" details
    And the user clicks on "Find a Table" button
    And the user clicks on a timeslot
    Then the user should be able to increment guests count

    Examples:
      | location | date       | timeSlot | guests |
      | LOC003   | 25-05-2027 | 15:45    | 1      |

  @regression
  Scenario Outline: Verify that message is shown after successful registration
    When the user selects the "<location>" "<date>" "<timeSlot>" "<guests>" details
    And the user clicks on "Find a Table" button
    And the user clicks on a timeslot
    #And the user increments the guests
    And the user clicks on "Make a Reservation" button
    Then the "Reservation Confirmed!" message is shown

    Examples:
      | location | date       | timeSlot | guests |
      | LOC003   | 22-05-2030 | 15:45     | 1      |