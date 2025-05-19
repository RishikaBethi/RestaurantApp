Feature: Verify the visibility of locations to the user

  Background:
    Given the user enters into the application
    When the user scrolls to the "locations" view
    Then the user should find the locations

  @smoke
  Scenario: Verify the visibility of description
   And the users should be able to see the locations description
   And the users should be able to see the "4.73" ratings

  @regression
  Scenario:Verify the service ratings
    And the user scrolls to the "Customer Reviews" view
    And verify the presence of ratings

  @regression
  Scenario: Verify the cuisine ratings
    And the user scrolls to the "Customer Reviews" view
    And the user clicks on "Cuisine Ratings" button
    And verify the presence of ratings

  @regression
  Scenario: Verify the filters of the service ratings
    And the user scrolls to the "Customer Reviews" view
    And the user clicks on "Top rated first" in dropDown
    Then the ratings should be displayed in the "Top rated first" order
    And the user clicks on "Low rated first" in dropDown
    Then the ratings should be displayed in the "Low rated first" order

  @regression
  Scenario: Verify the filters of the cuisine ratings
    And the user scrolls to the "Customer Reviews" view
    And the user clicks on "Cuisine Ratings" button
    And the user clicks on "Top rated first" in dropDown
    Then the ratings should be displayed in the "Top rated first" order
    And the user clicks on "Low rated first" in dropDown
    Then the ratings should be displayed in the "Low rated first" order