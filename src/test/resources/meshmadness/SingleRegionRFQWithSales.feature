Feature: User submit an RFQ into a single region that is actioned by sales

  Background:
    Propagation of an RFQ between two regions with sale people connected

  Scenario: A user connected to a region that has a sales and submits an RFQ and gets a quote
    Given the following users are logged in
    | Role   | Region |
    | User1  | SBP1   |
    | Sales1 | SBP1   |
    When users submit messages as follows
    | Role  | Message | Id |
    | User1 | StartRFQ|  1 |
    | Sales1| Quote   |  1 |
    Then the FSM looks like:
    | Count | Region | Id | State | Filler |
    | 1     | SBP1   | 1  | Quote | Sales1 |

  Scenario: A user connected to a region that has a sales and submits an RFQ and receives no quote
    Given the following users are logged in
    | Role   | Region |
    | User1  | SBP1   |
    | Sales1 | SBP1   |
    When users submit messages as follows
    | Role  | Message | Id |
    | User1 | StartRFQ| 1  |
    | Sales1| Putback | 1  |
    Then the FSM looks like:
    | Count | Region | Id | State   |
    | 1     | SBP1   | 1  | SendToDI|


  Scenario: A user connected to a region that has a sales and submits an RFQ and receives no quote after 3 negotiations
    Given the following users are logged in
    | Role   | Region |
    | User1  | SBP1   |
    | Sales1 | SBP1   |
    When users submit messages as follows
    | Role  | Message | Id |
    | User1 | StartRFQ|  1 |
    | Sales1| Putback |  1 |
    | Sales1| Putback |  1 |
    | Sales1| Putback |  1 |
    Then the FSM looks like:
    | Count | Region | Id | State   |
    | 3     | SBP1   | 1  | SendToDI|
    | 3     | SBP1   | 1  | Pickup  |

@focus
  Scenario: A user connected to a region that has two sales and submits an RFQ with no quote received back
    Given the following users are logged in
    | Role   | Region |
    | User1  | SBP1   |
    | Sales1 | SBP1   |
    | Sales2 | SBP1   |
    When users submit messages as follows
    | Role  | Message | Id |
    | User1 | StartRFQ|  1 |
    | Sales1| Putback |  1 |
    | Sales2| Putback |  1 |
    Then the FSM looks like:
    | Count | Region | Id | State   |
    | 3     | SBP1   | 1  | SendToDI|

  Scenario: A user connected to a region that has two sales and submits an RFQ and gets a quote back
    Given the following users are logged in
    | Role   | Region |
    | User1  | SBP1   |
    | Sales1 | SBP1   |
    | Sales2 | SBP1   |
    When users submit messages as follows
    | Role  | Message | Id |
    | User1 | StartRFQ|  1 |
    | Sales1| Quote   |  1 |
    Then the FSM looks like:
    | Count | Region | Id | State   | Filler|
    | 1     | SBP1   | 1  | SendToDI|       |
    | 1     | SBP1   | 1  | Quote   | Sales1|

  Scenario: A user connected to a region that has a sales and submits an RFQ and gets a quote
    Given the following users are logged in
    | Role   | Region |
    | User1  | SBP1   |
    | Sales1 | SBP1   |
    | Sales2 | SBP1   |
    | Sales3 | SBP1   |
    When users submit messages as follows
    | Role  | Message | Id |
    | User1 | StartRFQ|  1 |
    | Sales1| Putback |  1 |
    | Sales2| Putback |  1 |
    | Sales3| Putback |  1 |
    | Sales2| Quote   |  1 |
    Then the FSM looks like:
    | Count | Region | Id | State   | Filler|
    | 4     | SBP1   | 1  | SendToDI|       |
    | 1     | SBP1   | 1  | Quote   | Sales2|

  Scenario: A user initiates an RFQ with sales
    Given the following users are logged in
    | Role   | Region |
    | User1  | SBP1   |
    | Sales1 | SBP1   |
    When users submit messages as follows
    | Role  | Message | Id |
    | User1 | StartRFQ|  1 |
    | Sales1| Putback |  1 |
    | Sales1| Quote   |  1 |
    Then the FSM looks like:
    | Count | Region | Id | State   | Filler|
    | 2     | SBP1   | 1  | SendToDI|       |
    | 1     | SBP1   | 1  | Quote   | Sales1|
