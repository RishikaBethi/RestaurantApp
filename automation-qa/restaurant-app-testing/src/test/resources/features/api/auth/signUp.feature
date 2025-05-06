Feature: Signing up into the application

  Background:
    Given the base_uri of the application

  Scenario Outline: Sign Up user with valid credentials
    Given user sends a signup request with the following data
      | firstName   | lastName   | email   | password   |
      | <firstName> | <lastName> | <email> | <password> |
    When the user sends the post request to "/auth/sign-up" with the request payload
    Then the status code should be 201
    And the response should contain success "User registered successfully" message

    Examples:
      | firstName | lastName | email                   | password |
      | Sushma    | Ganta    | sushmaganta@example.com | Y!kjqKdX |


  Scenario Outline: Sign Up passing the already registered email
    Given user sends a signup request with the following data
      | firstName   | lastName   | email   | password   |
      | <firstName> | <lastName> | <email> | <password> |
    When the user sends the post request to "/auth/sign-up" with the request payload
    Then the status code should be 400
    And the response should contain failed "A user with this email address already exists" message

    Examples:
      | firstName | lastName | email               | password   |
      | Sindhu    | Reddy    | sushmag@example.com | Sindhu@123 |


  Scenario Outline: Verify Sign Up with missing and empty fields
    Given user sends a signup request with the following data
      | firstName   | lastName   | email   | password   |
      | <firstName> | <lastName> | <email> | <password> |
    When the user sends the post request to "/auth/sign-up" with the request payload
    Then the status code should be 400
    And the response should contain failed error "<error>" message

    Examples:
      | firstName | lastName  | email              | password   | error                                                                                         |
      |           | Gantagari | sushma@example.com | Sushma@123 | Invalid or missing first name                                                                 |
      | Sushma    |           | sushma@example.com | Sushma@123 | Invalid or missing last name                                                                  |
      | Sushma    | Gantagari |                    | Sushma@123 | Invalid email format                                                                          |
      | Sushma    | Gantagari | sushma@example.com |            | Password must be 8-16 characters, include uppercase, lowercase, number, and special character |
      | @empty    | Gantagari | sushma@example.com | Sushma@123 | Invalid or missing first name                                                                 |
      | Sushma    | @empty    | sushma@example.com | Sushma@123 | Invalid or missing last name                                                                  |
      | Sushma    | Gantagari | @empty             | Sushma@123 | Invalid email format                                                                          |
      | Sushma    | Gantagari | sushma@example.com | @empty     | Password must be 8-16 characters, include uppercase, lowercase, number, and special character |

  Scenario Outline: validate the invalid email format
    Given user sends a signup request with the following data
      | firstName   | lastName   | email   | password   |
      | <firstName> | <lastName> | <email> | <password> |
    When the user sends the post request to "/auth/sign-up" with the request payload
    Then the status code should be 400
    And the response should contain failed error "Invalid email format" message

    Examples:
      | firstName | lastName  | email     | password  |
      | sushma    | Gantaharo | ...@..com | Hello@123 |


  Scenario Outline: Sign Up with invalid password formats
    Given user sends a signup request with the following data
      | firstName   | lastName   | email   | password   |
      | <firstName> | <lastName> | <email> | <password> |
    When the user sends the post request to "/auth/sign-up" with the request payload
    Then the status code should be 400
    And the response should contain failed error "<error>" message

    Examples:

      | firstName | lastName  | email             | password              | error                                                                                         |
      | Sushma    | Gantagari | sushma@exampe.com | susha                 | Password must be 8-16 characters, include uppercase, lowercase, number, and special character |
      | Sushma    | Gantagari | sushma@exampe.com | sushma@123            | Password must be 8-16 characters, include uppercase, lowercase, number, and special character |
      | Sushma    | Gantagari | sushma@exampe.com | SUSHM@123             | Password must be 8-16 characters, include uppercase, lowercase, number, and special character |
      | Sushma    | Gantagari | sushma@exampe.com | Sushma123             | Password must be 8-16 characters, include uppercase, lowercase, number, and special character |
      | Sushma    | Gantagari | sushma@exampe.com | Sushma@asd            | Password must be 8-16 characters, include uppercase, lowercase, number, and special character |
      | Sushma    | Gantagari | sushma@exampe.com | Sushma@34565423456123 | Password must be 8-16 characters, include uppercase, lowercase, number, and special character |


  Scenario Outline: SignUp the user with exceeding character limits
    Given user sends a signup request with the following data
      | firstName   | lastName   | email   | password   |
      | <firstName> | <lastName> | <email> | <password> |
    When the user sends the post request to "/auth/sign-up" with the request payload
    Then the status code should be 400
    And the response should contain failed error "<error>" message

    Examples:
      | firstName                                                                                                                                                          | lastName                                                                                                                                                                                                           | email          | password  | error                         |
      | johnsaDFFFFFFFFFFFFFFFFFFFFWWWAaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa | Doe                                                                                                                                                                                                                | Jogn@wesf.com  | dffs@we2  | Invalid or missing first name |
      | John                                                                                                                                                               | doeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee | jogedw@234.com | gfgbSw22$ | Invalid or missing last name  |



