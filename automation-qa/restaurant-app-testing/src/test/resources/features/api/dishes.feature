Feature: Display the details for the users of locations
  
  Background: 
    Given the base_uri of the application
    
  Scenario: Display the popular dishes for authorized users
    When the user sends the authorized get request to "/dishes/popular" request payload
    Then the status code should be 200
    And the response should validate the "ValidatePopularDishes" schema

  Scenario: Display the popular dishes for unauthorized users
    When the user sends the get request to "/dishes/popular" request payload
    Then the status code should be 200
    And the response should validate the "ValidatePopularDishes" schema