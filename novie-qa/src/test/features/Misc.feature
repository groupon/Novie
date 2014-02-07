Feature: Misc tests
  Scenario: 1. Grouping parameter mandatory
    Given a user request Novie with a not existing endpoint
    Then verify http code is 404