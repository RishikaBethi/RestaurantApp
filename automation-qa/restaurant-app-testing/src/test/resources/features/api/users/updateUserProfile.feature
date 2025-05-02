Feature: Update the user profile

  Background:
    Given the base_uri of the application

  Scenario:
    Given the user is authenticated
    And the user sends the authorized get request to "/users/profile" request payload
    Then the status code should be the 200
    And the response should validate the "ValidateUserProfiles" schema


  Scenario:
    Given the user sends the authorized get request to "/users/profile" request payload
    Then the status code should be the 401
    And the response should contain failed error "Unauthorized: Email not found in token." message

  Scenario Outline:
    Given the user is authenticated
    And the user sends the profile update request with following data:
      | base64encodedImage   | firstName   | lastName   |
      | <base64encodedImage> | <firstName> | <lastName> |
    When the user sends the authorized put request to "/users/profile"
    Then the status code should be the 200
    And the response should contain success "Profile has been successfully updated" message


    Examples:
      | base64encodedImage                                                                           | firstName | lastName  |
      |                                                                                              | Sushmaa   | Gantagari |
      | iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII= | Sushmaa   | Gantagari |