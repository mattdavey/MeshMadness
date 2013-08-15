Feature: User submit an RFQ into a single region that is actioned by sales

  Background:
    Propagation of an RFQ between two regions with sale people connected

  Scenario: A user connected to a region that has a sales and submits an RFQ and gets a quote
    Given the following users are logged in
    | Role   | Region |
    | User1  | SBP1   |
    | Sales1 | SBP1   |
    When users submit messages as follows
    | Role  | Message |
    | User1 | StartRFQ|
    | Sales1| Quote   |
    Then the FSM looks like:
    | Count | Region | State |
    | 1     | SBP1   | Quote |

@focus
  Scenario: A user connected to a region that has a sales and submits an RFQ and receives no quote
    Given the following users are logged in
    | Role   | Region |
    | User1  | SBP1   |
    | Sales1 | SBP1   |
    When users submit messages as follows
    | Role  | Message |
    | User1 | StartRFQ|
    | Sales1| Putback |
    Then the FSM looks like:
    | Count | Region | State   |
    | 1     | SBP1   | SendToDI|

  Scenario: A user connected to a region that has two sales and submits an RFQ with no quote received back
    Given the following users are logged in
    | Role   | Region |
    | User1  | SBP1   |
    | Sales1 | SBP1   |
    | Sales2 | SBP1   |
    When users submit messages as follows
    | Role  | Message |
    | User1 | StartRFQ|
    | Sales1| Putback |
    | Sales2| Putback |
    Then the FSM looks like:
    | Count | Region | State   |
    | 1     | SBP1   | SendToDI|

  Scenario: A user connected to a region that has two sales and submits an RFQ and gets a quote back
    Given the following users are logged in
    | Role   | Region |
    | User1  | SBP1   |
    | Sales1 | SBP1   |
    | Sales2 | SBP1   |
    When users submit messages as follows
    | Role  | Message |
    | User1 | StartRFQ|
    | Sales1| Quote   |
    Then the FSM looks like:
    | Count | Region | State |
    | 1     | SBP1   | Quote |