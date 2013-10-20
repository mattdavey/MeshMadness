Feature: Submit an RFQ as a user with sales in five regions

  Background:
    Propagation of an RFQ between two regions with sale people connected

  Scenario: One user is logged in, RFQ is submitted and user gets a quote
    Given the following users are logged in
    | Role   | Region |
    | User1  | SBP1   |
    | Sales1 | SBP1   |
    | Sales2 | SBP2   |
    | Sales3 | SBP3   |
    When users submit messages as follows
    | Role  | Message | Id |
    | User1 | StartRFQ|  1 |
    | Sales1| Putback |  1 |
    | Sales2| Putback |  1 |
    | Sales3| Putback |  1 |
    | Sales2| Quote   |  1 |
    Then the FSM looks like:
    | Count | Region | Id | State | Filler|
    | 1     | SBP2   | 1  | Quote | Sales2|

  Scenario: Two users logged in, RFQ is submitted and user gets a quote
    Given the following users are logged in
    | Role   | Region |
    | User1  | SBP1   |
    | Sales1 | SBP1   |
    | Sales2 | SBP2   |
    | Sales3 | SBP3   |
    | User2  | SBP4   |
    When users submit messages as follows
    | Role  | Message | Id |
    | User1 | StartRFQ|  1 |
    | Sales1| Putback |  1 |
    | Sales2| Putback |  1 |
    | Sales3| Putback |  1 |
    | Sales2| Quote   |  1 |
    Then the FSM looks like:
    | Count | Region | Id | State | Filler|
    | 1     | SBP2   | 1  | Quote | Sales2|

  Scenario: RFQ is submitted and user gets a quote after multiple attempts
    Given the following users are logged in
    | Role   | Region |
    | User1  | SBP1   |
    | Sales1 | SBP1   |
    | Sales2 | SBP2   |
    | Sales3 | SBP3   |
    | User2  | SBP4   |
    When users submit messages as follows
    | Role  | Message | Id |
    | User1 | StartRFQ| 1  |
    | Sales1| Putback | 1  |
    | Sales2| Putback | 1  |
    | Sales3| Putback | 1  |
    | Sales1| Putback | 1  |
    | Sales2| Putback | 1  |
    | Sales3| Putback | 1  |
    | Sales2| Quote   | 1  |
    Then the FSM looks like:
    | Count | Region | Id | State | Filler|
    | 1     | SBP2   | 1  | Quote | Sales2|
