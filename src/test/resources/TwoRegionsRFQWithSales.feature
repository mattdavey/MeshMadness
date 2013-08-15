Feature: Submit an meshmadness.RFQ by users with sales in two regions

  Background:
    Propagation of an meshmadness.RFQ between two regions with sale people connected

  Scenario: A user connected to a region that has a sales and submits an meshmadness.RFQ and gets a quote
    Given the following users are logged in
    | Role   | Region |
    | User1  | SBP1   |
    | Sales1 | SBP1   |
    | Sales2 | SBP2   |
    When users submit messages as follows
    | Role  | Message |
    | User1 | StartRFQ|
    | Sales2| Quote   |
    Then the FSM looks like:
    | Count | Region | State |
    | 1     | SBP2   | Quote |

  Scenario: A user connected to a region that has a sales and submits an meshmadness.RFQ and receives no quote
    Given the following users are logged in
    | Role   | Region |
    | User1  | SBP1   |
    | Sales1 | SBP1   |
    | Sales2 | SBP2   |
    When users submit messages as follows
    | Role  | Message |
    | User1 | StartRFQ|
    | Sales2| Putback |
    Then the FSM looks like:
    | Count | Region | State   |
    | 1     | SBP2   | SendToDI|

  Scenario: A user connected to a region that has two sales and submits an meshmadness.RFQ with no quote received back
    Given the following users are logged in
    | Role   | Region |
    | User1  | SBP1   |
    | Sales1 | SBP1   |
    | Sales2 | SBP2   |
    When users submit messages as follows
    | Role  | Message |
    | User1 | StartRFQ|
    | Sales1| Putback |
    | Sales2| Putback |
    Then the FSM looks like:
    | Count | Region | State   |
    | 1     | SBP1   | SendToDI|

  Scenario: A user connected to a region that has two sales and submits an meshmadness.RFQ and gets a quote back
    Given the following users are logged in
    | Role   | Region |
    | User1  | SBP1   |
    | Sales1 | SBP1   |
    | Sales2 | SBP2   |
    When users submit messages as follows
    | Role  | Message |
    | User1 | StartRFQ|
    | Sales1| Quote   |
    Then the FSM looks like:
    | Count | Region | State |
    | 1     | SBP1   | Quote |