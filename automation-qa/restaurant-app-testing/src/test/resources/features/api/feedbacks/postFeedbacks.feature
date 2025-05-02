Feature: Verify the feedbacks of the user

  Background:
    Given the base_uri of the application
    Given user sends a sign in request with the following data
      | email               | password     |
      | sushmag@example.com | Password123! |
    When the user sends the post request to "/auth/sign-in" with the request payload
    Then the status code should be 201
    And the token is stored

  Scenario Outline: Verify the successful feedback posted with valid details
    Given the user sends a feedback request with the following data:
      | cuisineComment   | cuisineRating   | reservationId   | serviceComment   | serviceRating   |
      | <cuisineComment> | <cuisineRating> | <reservationId> | <serviceComment> | <serviceRating> |
    When the user sends the authorized post request to "/feedbacks" with the request payload
    Then the status code should be 201
    And the response should contain success "Feedback has been created" message
    Examples:
      | cuisineComment | cuisineRating | reservationId                        | serviceComment | serviceRating |
      | Good food      | 4             | 91fb59a8-0532-413c-8197-1f3f0a5fcbec | good           | 4             |
      | Bad food       | 3             | 91fb59a8-0532-413c-8197-1f3f0a5fcbec |                |               |
      |                |               | 91fb59a8-0532-413c-8197-1f3f0a5fcbec | bad            | 2             |
      |                | 4             | 91fb59a8-0532-413c-8197-1f3f0a5fcbec |                | 4             |
      |                | 4             | 91fb59a8-0532-413c-8197-1f3f0a5fcbec |                |               |
      |                |               | 91fb59a8-0532-413c-8197-1f3f0a5fcbec |                | 4             |


  Scenario Outline: Verify the failed feedbacks with invalid details
    Given the user sends a feedback request with the following data:
      | cuisineComment   | cuisineRating   | reservationId   | serviceComment   | serviceRating   |
      | <cuisineComment> | <cuisineRating> | <reservationId> | <serviceComment> | <serviceRating> |
    When the user sends the authorized post request to "/feedbacks" with the request payload
    Then the status code should be 400
    And the response should contain failed error "<error>" message

    Examples:
      | cuisineComment | cuisineRating | reservationId                        | serviceComment | serviceRating | error                                                                              |
      | Good food      |               | 91fb59a8-0532-413c-8197-1f3f0a5fcbec | good           |               | Service rating is required when service comment is provided                        |
      | Bad food       | 3             | 23456-uyfdscv-fgyh-geds              |                |               | Reservation not found                                                              |
      | Good           | 3             | 15b306d8-521d-471e-ad28-78b905af24cc | bad            | 2             | Feedback can be provided only once your reservation is in progress or has finished |
      | Good           | 4             | 15b306d8-521d-471e-ad28-78b905af24cc | Good           | 4             | Feedback can be provided only once your reservation is in progress or has finished |





