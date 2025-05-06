Feature: Get Available Tables for a Valid Request

  Background:

    Given the base_uri of the application
    And the user is authenticated

  Scenario Outline: Verify return of list of available tables based on valid filters
    When the user sends the authorized get request to "/bookings/tables?locationId=<locationId>&date=<date>&time=<time>&guests=<guests>" request payload
    Then the status code should be 200
    And the response should validate the "ValidateTables" schema

    Examples:
      | locationId | date       | time  | guests |
      | LOC001     | 2025-08-02 | 15:00 | 4      |

  Scenario Outline: Verify response for invalid filters
    When the user sends the authorized get request to "/bookings/tables?locationId=<locationId>&date=<date>&time=<time>&guests=<guests>" request payload
    Then the status code should be 400
    And the response should contain failed error <message> message

    Examples:
      | locationId | date       | time  | guests | message                                             |
      | LOC001     | 2023-02-06 | 13:00 | 6      | "Date/time cannot be selected in the past"          |
      | LOC001     | 21-21-2025 | 10:00 | 8      | "Invalid date format. Use YYYY-MM-DD"               |
      | LOC001     | 2025-08-02 | 45:00 | 4      | "Invalid time format. Use HH:MM"                    |
      | LOC001     | 2025-08-02 | 12:00 | four   | "Invalid guest capacity format. Must be an integer" |


  Scenario Outline: Verify booking by clients

    Given a user sends booking request with following data
      | locationId   | tableNumber   | date   | guestsNumber   | timeFrom   | timeTo   |
      | <locationId> | <tableNumber> | <date> | <guestsNumber> | <timeFrom> | <timeTo> |
    When the user sends the authorized post request to "/bookings/client" with the request payload
    Then the status code should be <status>

    Examples:
      | locationId | tableNumber | date       | guestsNumber | timeFrom | timeTo | status |
      | LOC001     | 3           | 2025-08-02 | 12           | 10:00    | 11:30  | 409    |
      | LOC001     | 9999        | 2025-08-02 | 4            | 10:00    | 11:30  | 400    |
      | LOC001     | 1           | 2023-02-06 | 6            | 12:00    | 1:30   | 400    |

  Scenario Outline: Verify failed booking by clients for missing fields
    Given a user sends booking request with following data
      | locationId   | tableNumber   | date   | guestsNumber   | timeFrom   | timeTo   |
      | <locationId> | <tableNumber> | <date> | <guestsNumber> | <timeFrom> | <timeTo> |
    When the user sends the authorized post request to "/bookings/client" with the request payload
    Then the status code should be <status>
    And the response should contain failed error <message> message

    Examples:
      | locationId | tableNumber | date       | guestsNumber | timeFrom | timeTo | status | message                                |
      |            | 2           | 2025-08-02 | 3            | 10:00    | 11:30  | 400    | "Missing required field: locationId"   |
      | LOC001     |             | 2025-08-02 | 8            | 12:00    | 1:30   | 400    | "Missing required field: tableNumber"  |
      | LOC001     | 2           |            | 4            | 10:00    | 11:30  | 400    | "Missing required field: date"         |
      | LOC001     | 2           | 2025-08-02 |              | 10:00    | 12:30  | 400    | "Missing required field: guestsNumber" |
      | LOC001     | 3           | 2025-08-02 | 5            |          | 12:00  | 400    | "Missing required field: timeFrom"     |
      | LOC001     | 2           | 2025-08-02 | 4            | 12:00    |        | 400    | "Missing required field: timeTo"       |