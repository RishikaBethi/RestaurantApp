Feature: Verify the reservations by the waiter

  Background:
    Given the user enters into the application
    And the user enters into the sign in page
    When the user enters the "vardhini@example.com" and "Password123!"
    And the user clicks on "sign in" button
    Then the user will be redirected to the "http://team-7-frontend-bucket.s3-website-ap-southeast-2.amazonaws.com/waiter-reservations" page

  Scenario: Verify the proper filtering of reservations by the waiter
    When the user clicks on "WaiterReservations" button
    And the waiter selects the date "20-03-2026"
    And the user clicks on "waiterReservationsSearch" button
    Then the waiter should able to see reservations


  Scenario: Valid Reservation by waiter for visitor
    When the user clicks on "WaiterReservations" button
    And the user clicks on "CreateNewReservation" button
    And the waiter selects the "21-05-2026" for "date"
    And the user clicks on "Visitor" button
    And the waiter selects the guests
    And the waiter selects the "10:30" for "time"
    And the waiter selects the "Table 3" for "table"
    And the user clicks on "Make a waiter Reservation" button
    Then the page will display the confirmation "Reservation made successfully!" message


  Scenario: Valid Reservation by waiter for existing customer
    When the user clicks on "WaiterReservations" button
    And the user clicks on "CreateNewReservation" button
    And the waiter selects the "06-05-2025" for "date"
    And the user clicks on "Customer" button
    And the waiter sends the "sushmag@example.com" customer email
    And the waiter selects the guests
    And the waiter selects the "10:30" for "time"
    And the waiter selects the "Table 3" for "table"
    And the user clicks on "Make a waiter Reservation" button
    Then the page will display the confirmation "Reservation made successfully!" message