Feature: Novie with a single group on application
  Scenario: 1. empty result JSON
    Given A clean database.
    And Dataset src/test/resources/qa_dataset_1.sql loaded.
    And a user request Novie with following parameters:
      |*NAME*     | *VALUE*     |
      |group      | application |
      |application| Groupon     |
    Then verify http code is 200
    And verify that the Content-Type is "application/json"
    And verify, in json, that the total number of record is 0

  Scenario: 2. Non empty result JSON
    Given A clean database.
    And Dataset src/test/resources/qa_dataset_1.sql loaded.
    And a user request Novie with following parameters:
      |*NAME*     | *VALUE*     |
      |group      | application |
      |application| github     |
    Then verify http code is 200
    And verify that the Content-Type is "application/json"
    And verify, in json, that the total number of record is 1

  Scenario: 3. Search with empty string
    Given A clean database.
    And Dataset src/test/resources/qa_dataset_1.sql loaded.
    And a user request Novie with following parameters:
      |*NAME*     | *VALUE*     |
      |group          | application |
      |application.url|             |
    Then verify http code is 200
    And verify that the Content-Type is "application/json"
    And verify, in json, that the total number of record is 1

  Scenario: 4. Search with empty string
    Given A clean database.
    And Dataset src/test/resources/qa_dataset_1.sql loaded.
    And a user request Novie with following parameters:
      |*NAME*         | *VALUE*       |
      |group          | application   |
      |application.url| !             |
    Then verify http code is 200
    And verify that the Content-Type is "application/json"
    And verify, in json, that the total number of record is 3