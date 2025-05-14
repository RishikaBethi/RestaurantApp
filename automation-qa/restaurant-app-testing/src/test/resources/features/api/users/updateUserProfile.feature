Feature: Update the user profile

  Background:
    Given the base_uri of the application

    @smoke
  Scenario: Get the details of the authorized user
    Given the user is authenticated
    And the user sends the authorized get request to "/users/profile" request payload
    Then the status code should be the 200

  @regression
  Scenario: Get the details of the authorized user
    Given the user is authenticated
    And the user sends the authorized get request to "/users/profile" request payload
    Then the status code should be the 200
    And the response should validate the "ValidateUserProfiles" schema

  @regression
  Scenario: Verify the error for the get request for the unauthorized users
    Given the user sends the get request to "/users/profile" request payload
    Then the status code should be the 401
    And the response should contain failed error "Unauthorized: Email not found in token." message

  @regression
  Scenario Outline: Trying to update the profile by authorized user
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
      | iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAABFUlEQVR42mNgGAXUBwAADNIBzUHiPIBMA8QBJZIDNQDk3oFwD8RuAAhKgJgBlJ6DUAxEyQyQG0gXifMBMZzAMkPoA4kxAaQmgnyBlgcgxkmAPAFNggQZsZgPwDkHmBiA/iVYB8gpgHksygPIWQj0DWA2QdEDYVgHx2RDZCFA9k0kB5CIiD4GoNIFhFGBuBGQgxHwHiMmCMQsoAVBKAtQTIBxAywLoAxHyNqCpAuRLgJYEjQBMEzQDoJoBXDbANkDYkODJAAAwB2rhAK/V+X6YAAAAAElFTkSuQmCC | Sushma    | G      |
