Feature: Find the available reservations for required time slots

  Background:
    Given the user enters into the application
    And the user clicks on "Book a Table" button

  Scenario: Verify the availability of tables for default time and location
    When the user selects the "19:15" timeslot
    And the user clicks on "Find a Table" button
    Then the user should be able to see the available tables

  Scenario Outline: Verify the availability of tables for valid location date and time
    When the user selects the "<location>" "<date>" "<timeSlot>" "<guests>" details
    And the user clicks on "Find a Table" button
    Then the user should be able to see the available tables

    Examples:
      | location | date       | timeSlot | guests |
      | LOC003   | 21-05-2025 | 10:30    | 1      |
      | LOC003   | 21-05-2025 | 12:15    | 2      |
      | LOC003   | 21-05-2025 | 14:00    | 3      |
      | LOC003   | 21-05-2025 | 15:45    | 4      |
      | LOC003   | 21-05-2025 | 17:30    | 5      |
      | LOC003   | 21-05-2025 | 19:15    | 8      |
      | LOC003   | 21-05-2025 | 21:00    | 10     |
      | LOC001   | 21-05-2025 | 10:30    | 1      |
      | LOC001   | 21-05-2025 | 12:15    | 2      |
      | LOC001   | 21-05-2025 | 14:00    | 3      |
      | LOC001   | 21-05-2025 | 15:45    | 4      |
      | LOC001   | 21-05-2025 | 17:30    | 5      |
      | LOC001   | 21-05-2025 | 19:15    | 8      |
      | LOC001   | 21-05-2025 | 21:00    | 10     |
      | LOC002   | 21-05-2025 | 10:30    | 1      |
      | LOC002   | 21-05-2025 | 12:15    | 2      |
      | LOC002   | 21-05-2025 | 14:00    | 3      |
      | LOC002   | 21-05-2025 | 15:45    | 4      |
      | LOC002   | 21-05-2025 | 17:30    | 5      |
      | LOC002   | 21-05-2025 | 19:15    | 8      |
      | LOC002   | 21-05-2025 | 21:00    | 10     |

  Scenario Outline: Verify the error message if invalid details are sent
    When the user selects the "<location>" "<date>" "<timeSlot>" "<guests>" details
    And the user clicks on "Find a Table" button
    Then the page will display the error "<error>" message
    And the user should be not see any available tables

    Examples:
      | location | date       | timeSlot | guests | error                                                      |
      | LOC001   | 07-04-2025 | 10:30    | 3      | Date/time cannot be selected in the past                   |
      | LOC001   |            |          | 3      | Date/time cannot be selected in the past                   |
      | LOC001   | 07-04-2026 | 10:30    | 0      | Please enter a valid number of guests (minimum 1).         |
      | LOC001   | 07-04-2026 | 10:30    | 23     | We are sorry! We couldn't find tables as per your criteria |

