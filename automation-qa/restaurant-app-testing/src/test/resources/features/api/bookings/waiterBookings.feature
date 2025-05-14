Feature: Verify operations on waiter bookings

  Background:
    Given the base_uri of the application
    Given user sends a sign in request with the following data
      | email                | password     |
      | vardhini@example.com | Password123! |
    When the user sends the post request to "/auth/sign-in" with the request payload
    Then the status code should be 201
    And the waiter token is stored

    @smoke
  Scenario Outline: Verify successful waiter registration for existing customers
    Given a waiter sends booking request with following data
      | clientType   | customerEmail   | date   | guestsNumber   | locationId   | tableNumber   | timeFrom   | timeTo   |
      | <clientType> | <customerEmail> | <date> | <guestsNumber> | <locationId> | <tableNumber> | <timeFrom> | <timeTo> |
    When the waiter sends authorized post request to "/bookings/waiter" request payload
    Then the status code should be 201
    Examples:
      | clientType | customerEmail       | date       | guestsNumber | locationId | tableNumber | timeFrom   | timeTo   |
      | CUSTOMER   | sushmag@example.com | 2025-08-22 | 4            | LOC003     | 3           | 15:00      | 16:00    |
      | VISITOR    | John@example.com    | 2025-08-29 | 4            | LOC003     | 2           | <timeFrom> | <timeTo> |

      @regression
  Scenario Outline: Verify successful waiter registration for existing customers
    Given a waiter sends booking request with following data
      | clientType   | customerEmail   | date   | guestsNumber   | locationId   | tableNumber   | timeFrom   | timeTo   |
      | <clientType> | <customerEmail> | <date> | <guestsNumber> | <locationId> | <tableNumber> | <timeFrom> | <timeTo> |
    When the waiter sends authorized post request to "/bookings/waiter" request payload
    Then the status code should be 201
    And the response should validate the "ValidateWaiterBookings" schema

    Examples:
      | clientType | customerEmail       | date       | guestsNumber | locationId | tableNumber | timeFrom | timeTo |
      | CUSTOMER   | sushmag@example.com | 2025-08-22 | 4            | LOC003     | 3           | 15:00    | 16:00  |
      | VISITOR    | John@example.com    | 2025-08-29 | 4            | LOC003     | 2           | 12:00    | 14:00  |

  @regression
  Scenario Outline: Verify conflict in registration for already booked tables
    Given a waiter sends booking request with following data
      | clientType   | customerEmail   | date   | guestsNumber   | locationId   | tableNumber   | timeFrom   | timeTo   |
      | <clientType> | <customerEmail> | <date> | <guestsNumber> | <locationId> | <tableNumber> | <timeFrom> | <timeTo> |
    When the waiter sends authorized post request to "/bookings/waiter" request payload
    Then the status code should be 409

    Examples:
      | clientType | customerEmail       | date       | guestsNumber | locationId | tableNumber | timeFrom | timeTo |
      | CUSTOMER   | sushmag@example.com | 2025-08-22 | 4            | LOC003     | 3           | 15:00    | 16:00  |

  @regression
  Scenario Outline: Verify the reservation cannot be made without giving table number
    Given a waiter sends booking request with following data
      | clientType   | customerEmail   | date   | guestsNumber   | locationId   | tableNumber   | timeFrom   | timeTo   |
      | <clientType> | <customerEmail> | <date> | <guestsNumber> | <locationId> | <tableNumber> | <timeFrom> | <timeTo> |
    When the waiter sends authorized post request to "/bookings/waiter" request payload
    Then the status code should be 400
    And the response should contain failed error "<message>" message

    Examples:
      | clientType | customerEmail       | date       | guestsNumber | locationId | tableNumber | timeFrom | timeTo | message                                            |
      | CUSTOMER   | sushmag@example.com | 2025-08-25 | 4            | LOC003     |             | 15:00    | 16:00  | Missing required field: tableNumber                |
      | CUSTOMER   | sushmag@example.com | 2025-08-29 | 4            | LOC003     | 999999      | 15:00    | 16:00  | Specified table does not exist.                    |
      | CUSTOMER   | sushmag@example.com | 2023-08-29 | 4            | LOC003     | 2           | 15:00    | 16:00  | Reservation cannot be made for a past time.        |
      | CUSTOMER   | sushmag@example.com | 2027-08-29 | 4            | LOC003     | 2           | 45:00    | 47:00  | Invalid date/time format. Use yyyy-MM-dd and HH:mm |

  @regression
  Scenario Outline: Verify waiter cannot reserve a table at a non-assigned location
    Given a waiter sends booking request with following data
      | clientType   | customerEmail   | date   | guestsNumber   | locationId   | tableNumber   | timeFrom   | timeTo   |
      | <clientType> | <customerEmail> | <date> | <guestsNumber> | <locationId> | <tableNumber> | <timeFrom> | <timeTo> |
    When the waiter sends authorized post request to "/bookings/waiter" request payload
    Then the status code should be 201

    Examples:
      | clientType | customerEmail       | date       | guestsNumber | locationId | tableNumber | timeFrom | timeTo | message                             |
      | CUSTOMER   | sushmag@example.com | 2025-08-25 | 4            | LOC001     | 2           | 15:00    | 16:00  | Missing required field: tableNumber |
