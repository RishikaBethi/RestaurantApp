Feature: Get dishes by specifying type and sort

  Background:
    Given the base_uri of the application
    And the user is authenticated

    @smoke
  Scenario Outline: Display dishes based on criteria
    When the user sends the authorized get request to "dishes?<dishType>&<sort>" request payload
    Then the status code should be 200
    Examples:
      | dishType              | sort                 |
      | dishType=DESSERTS     | sort=price-asc       |

      @regression
  Scenario Outline: Display dishes based on criteria
    When the user sends the authorized get request to "dishes?<dishType>&<sort>" request payload
    Then the status code should be 200
    And the response should validate the "ValidateDishes" schema

    Examples:
      | dishType              | sort                 |
      | dishType=DESSERTS     | sort=price-asc       |
      | dishType=DESSERTS     | sort=price-desc      |
      | dishType=DESSERTS     | sort=popularity-asc  |
      | dishType=DESSERTS     | sort=popularity-desc |
      | dishType=APPETIZERS   | sort=price-asc       |
      | dishType=APPETIZERS   | sort=price-desc      |
      | dishType=APPETIZERS   | sort=popularity-asc  |
      | dishType=APPETIZERS   | sort=popularity-desc |
      | dishType=MAIN COURSES | sort=price-asc       |
      | dishType=MAIN COURSES | sort=price-desc      |
      | dishType=MAIN COURSES | sort=popularity-asc  |
      | dishType=MAIN COURSES | sort=popularity-desc |
      | @empty                | sort=price-asc       |
      | dishType=DESSERTS     |                      |

  @regression
  Scenario Outline: Display error message when invalid filter types are applied
    When the user sends the authorized get request to "/dishes?dishType=<dishType>&sortBy=<sort>" request payload
    Then the status code should be 400
    And the response should contain failed error <message> message

    Examples:
      | dishType | sort      | message                                                                                       |
      | abc      | price-asc | "Invalid dishType: abc. Must be MAIN COURSES, DESSERTS, or APPETIZERS"                        |
      | DESSERTS | abc       | "Invalid sortBy format: abc. Use format field-direction (e.g., price-asc or popularity-desc)" |

  @regression
  Scenario Outline: Display dishes by id
    When the user sends the authorized get request to "/dishes/<id>" request payload
    Then the status code should be <status>

    Examples:
      | id   | status |
      | D103 | 200    |
      | 9999 | 404    |