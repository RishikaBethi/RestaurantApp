Feature: Handle user profile updating

  Background:
    Given the user enters into the application
    Given the user enters into the sign in page
    When the user enters the "rg@email.com" and "Password123!"
    And the user clicks on "sign in" button
    And the user clicks on profile

  @smoke
  Scenario Outline: Update profile with valid data
    When the user enter "<firstName>" and "<lastName>"
    And the user clicks on "Save Changes" button
    Then the page will display the successfully updated message

    Examples:
      | firstName | lastName |
      | Rishitha  | G        |

  @regression
  Scenario Outline: Update profile with valid data and image
    When the user enter "<firstName>" and "<lastName>"
    And the user uploads the image "C:\Users\gottiparthy_rishitha\Desktop\b31257b43245c117758f79dc8758eda3.jpg"
    And the user clicks on "Save Changes" button
    Then the page will display the successfully updated message

    Examples:
      | firstName | lastName |
      | Rishitha  | G        |

  @regression
  Scenario Outline: Update profile with exceeding lengths in fields
    When the user enter "<firstName>" and "<lastName>"
    Then the user should see a message that the fields exceed maximum limit

  Examples:
    | firstName                                            | lastName |
    | qwertyuiopasdfghjklzxcvbnmqwertyuiopasdfghjklzxcvbnm | G        |

  @regression
  Scenario Outline: Unsuccessful password update
    When the user clicks on "Change Password" button
    And the user enters "<oldPassword>", "<newPassword>" and "<confirmPassword>"
    And the user clicks on "Save Changes" button
    Then the page will display "Incorrect old password" message

    Examples:
      | oldPassword | newPassword  | confirmPassword |
      | abc         | Rishitha123! | Rishitha123!    |

  @regression
  Scenario Outline: Missing password character criteria
    When the user clicks on "Change Password" button
    And the user enters "<oldPassword>", "<newPassword>" and "<confirmPassword>"
    Then the page will display error "<message>"

    Examples:
      | oldPassword  | newPassword  | confirmPassword | message                                    |
      | Password123! | password123! | password123!    | At least one uppercase letter required     |
      | Password123! | PASSWORD123! | PASSWORD123!    | At least one lowercase letter required     |
      | Password123! | Password!    | Password!       | At least one number required               |
      | Password123! | Password123  | Password123     | At least one special character required    |
      | Password123! | Pass12!      | Pass12!         | Password must be 8-16 characters long      |
      | Password123! | Password123! | Password123!    | New password should not match old password |

  @regression
  Scenario Outline: New password and confirm password mismatch
    When the user clicks on "Change Password" button
    And the user enters "<oldPassword>", "<newPassword>" and "<confirmPassword>"
    Then the page will display "<message>"

    Examples:
      | oldPassword  | newPassword | confirmPassword | message                                  |
      | Password123! | Abcdef123!  | Abcdef123       | Confirm password must match new password |
