Feature: Novie default parameters
  Scenario: 1. Grouping parameter mandatory
    Given a user request Novie with no parameter
    Then verify http code is 400
    And verify http error message is "Missing mandatory field group"

  Scenario: 2. Mandatory dimension is present
    Given a user request Novie with following parameters:
    |*NAME*     | *VALUE*     |
    |group      | application |
    Then verify http code is 400
    And verify http error message is "Dimension APPLICATION is mandatory."

  Scenario: 3. Invalid parameter is not supported
    Given a user request Novie with following parameters:
      |*NAME*     | *VALUE*     |
      |group      | application |
      |fakeParam  | test2       |
    Then verify http code is 400
    And verify http error message is "Invalid parameter fakeParam for endpoint app"

  Scenario: 4. Mandatory dimension is present
    Given A clean database.
    And Dataset src/test/resources/qa_dataset_1.sql loaded.
    Given a user request Novie with following parameters:
      |*NAME*     | *VALUE*     |
      |group      | application |
      |application| Groupon     |
    Then verify http code is 200

  Scenario: 5. Unknown extension
    Given A clean database.
    And Dataset src/test/resources/qa_dataset_1.sql loaded.
    Given a user request Novie in "JSON" with following parameters:
      |*NAME*     | *VALUE*     |
      |group      | application |
      |application| Groupon     |
    Then verify http code is 404

  Scenario Outline: 6. Check all response content-type depending on the url extension
    Given A clean database.
    And Dataset src/test/resources/qa_dataset_1.sql loaded.
    Given a user request Novie in "<EXTENSION>" with following parameters:
      |*NAME*     | *VALUE*     |
      |group      | application |
      |application| Groupon     |
    Then verify http code is 200
    And verify that the Content-Type is "<CONTENTTYPE>"

  Examples:
    |EXTENSION  | CONTENTTYPE      |
    |json       | application/json       |
    |xml        | application/xml        |
    |csv        | text/csv;charset=UTF-8 |

