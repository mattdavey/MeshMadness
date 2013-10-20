Feature: Submit an RFQ by users with sales in two regions

  Background:
    Propagation of an RFQ between two regions with sale people connected

  Scenario: A user connected to a region that has a sales and submits an RFQ and gets a quote
    Given the following users are logged in
    | Role   | Region |
    | User1  | SBP1   |
    | Sales1 | SBP1   |
    | Sales2 | SBP2   |
    When users submit messages as follows
    | Role  | Message | Id |
    | User1 | StartRFQ|  1 |
    | Sales2| Quote   |  1 |
    Then the FSM looks like:
    | Count | Region | Id | State | Filler|
    | 1     | SBP2   | 1  | Quote | Sales2|

  Scenario: A user connected to a region that has sales and submits an RFQ and receives no quote
    Given the following users are logged in
    | Role   | Region |
    | User1  | SBP1   |
    | Sales1 | SBP1   |
    | Sales2 | SBP2   |
    When users submit messages as follows
    | Role  | Message | Id |
    | User1 | StartRFQ|  1 |
    | Sales2| Putback |  1 |
    Then the FSM looks like:
    | Count | Region | Id | State   |
    | 1     | SBP2   | 1  | SendToDI|

  Scenario: A user connected to a region that has two sales and submits an RFQ with no quote received back
    Given the following users are logged in
    | Role   | Region |
    | User1  | SBP1   |
    | Sales1 | SBP1   |
    | Sales2 | SBP2   |
    When users submit messages as follows
    | Role  | Message | Id |
    | User1 | StartRFQ|  1 |
    | Sales1| Putback |  1 |
    | Sales2| Putback |  1 |
    Then the FSM looks like:
    | Count | Region | Id | State   |
    | 1     | SBP1   | 1  | SendToDI|

  Scenario: A user connected to a region that has two sales and submits an RFQ and gets a quote back
    Given the following users are logged in
    | Role   | Region |
    | User1  | SBP1   |
    | Sales1 | SBP1   |
    | Sales2 | SBP2   |
    When users submit messages as follows
    | Role  | Message | Id |
    | User1 | StartRFQ|  1 |
    | Sales1| Quote   |  1 |
    Then the FSM looks like:
    | Count | Region | Id | State | Filler|
    | 1     | SBP1   | 1  | Quote | Sales1|