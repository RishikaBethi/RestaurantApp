Feature: Testing Unauthorized requests
  Background:
    Given the base_uri of the application

  Scenario Outline: Verify unauthorized users cannot delete reservations
    When the user sends the delete request to "/reservations/<id>" request payload
    Then the status code should be 401

    Examples:
      | id                                   |
      | ebd63de3-4aaa-4539-8a27-1be7d2b22d67 |