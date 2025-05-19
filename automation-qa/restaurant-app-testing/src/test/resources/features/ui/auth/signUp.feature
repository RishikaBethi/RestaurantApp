Feature: Verify the registration of the user

  Background:
    Given the user enters into the application
    And the user enters into the sign in page
    And the user clicks on "create an account link" button

  @smoke
  Scenario Outline: Verify the valid registration of the user
    When the user enters the following data:
      | firstName   | lastName   | email   | password   | confirmPassword   |
      | <firstName> | <lastName> | <email> | <password> | <confirmPassword> |
    And the user clicks on "create an account" button
    Then the user will be redirected to the "<signIn>" page

    Examples:
      | firstName | lastName | email               | password     | confirmPassword | signIn                                                                      |
      | Sushma    | G12      | sushmaaag@gmail.com | Password123! | Password123!    | http://team-7-frontend-bucket.s3-website-ap-southeast-2.amazonaws.com/login |

  @regression
  Scenario Outline: Verify the registration by entering invalid details
    When the user enters the following data:
      | firstName   | lastName   | email   | password   | confirmPassword   |
      | <firstName> | <lastName> | <email> | <password> | <confirmPassword> |
    And the user clicks on "create an account" button
    Then the page will display the registration error "<error>" message

    Examples:

      | firstName | lastName | email           | password     | confirmPassword | error                                                                                                                   |
      | JohnSon~  | Doe      | john@gmail.com  | Password123! | Password123!    | First name must start with a letter and be up to 50 characters.Only letters,special characters and numbers are allowed. |
      | 1Johnson  | Doe      | john@gmail.com  | Password123! | Password123!    | First name must start with a letter and be up to 50 characters.Only letters,special characters and numbers are allowed. |
      | @Johnson  | Doe      | john@gmail.com  | Password123! | Password123!    | First name must start with a letter and be up to 50 characters.Only letters,special characters and numbers are allowed. |
      | ~Johnson  | Doe      | john@gmail.com  | Password123! | Password123!    | First name must start with a letter and be up to 50 characters.Only letters,special characters and numbers are allowed. |
      | ~Johnson~ | Doe      | john@gmail.com  | Password123! | Password123!    | First name must start with a letter and be up to 50 characters.Only letters,special characters and numbers are allowed. |
      | Johnson   | Doe~     | john@gmail.com  | Password123! | Password123!    | Last name can include letters, numbers, and special characters, up to 50 characters.                                    |
      | Johnson   | ~Doe     | john@gmail.com  | Password123! | Password123!    | Last name can include letters, numbers, and special characters, up to 50 characters.                                    |
      | Johnson   | ~Doe     | john@gmail.com  | Password123! | Password123!    | Last name can include letters, numbers, and special characters, up to 50 characters.                                    |
      | Johnson   | ~Doe~    | john@gmail.com  | Password123! | Password123!    | Last name can include letters, numbers, and special characters, up to 50 characters.                                    |
      | Johnson   | Doe      | john@@gmail.com | Password123! | Password123!    | Invalid email address. Ensure the username is alphanumeric, may contain '-' or '_', and the domain is valid.            |
      | Johnson   | Doe      | john@gmail.com  | Password123! | password123!    | Confirm password must match new password                                                                                |

  @regression
  Scenario Outline: Verify the registration by entering invalid passwords
    When the user enters the following data:
      | firstName   | lastName   | email   | password   | confirmPassword   |
      | <firstName> | <lastName> | <email> | <password> | <confirmPassword> |
    And the user clicks on "create an account" button
    Then the page will display the password error "<error>" message

    Examples:

      | firstName | lastName | email          | password                         | confirmPassword                  | error                                          |
      | Johnson   | Doe      | john@gmail.com | password123!                     | password123!                     | At least one uppercase letter required         |
      | Johnson   | Doe      | john@gmail.com | PASSWORD123!                     | PASSWORD123!                     | At least one lowercase letter required         |
      | Johnson   | Doe      | john@gmail.com | Password!                        | Password!                        | At least one number required                   |
      | Johnson   | Doe      | john@gmail.com | Password123                      | Password123                      | At least one special character required        |
      | Johnson   | Doe      | john@gmail.com | Pa3!                             | Pa3!                             | Password must be at least 8-16 characters long |
      | Johnson   | Doe      | john@gmail.com | Password12345678910111213141516! | Password12345678910111213141516! | Password must be at least 8-16 characters long |