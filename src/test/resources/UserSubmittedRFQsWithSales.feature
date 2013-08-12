Feature: Submit an RFQ by users with sales connected to regions

  Background:
    Propagation of an RFQ between two regions with sale people connected

  Scenario: A user connected to a region that has a salesperson that will Qupte and submits an RFQ
    Given the following users are logged in
    | Role   | Region | Dialog  |
    | User1  | SBP1   |         |
    | Sales1 | SBP1   | Quote   |
    When users submit messages as follows
    | Role  | Region |
    | User1 | SBP1   |
    Then the FSM looks like:
    | Count | Region | State     |
    | 1     | SBP1   | SendPrice |

  Scenario: A user connected to a region that has a salesperson that will Putback and submits an RFQ
    Given the following users are logged in
    | Role   | Region | Dialog  |
    | User1  | SBP1   |         |
    | Sales1 | SBP1   | Putback |
    When users submit messages as follows
    | Role  | Region |
    | User1 | SBP1   |
    Then the FSM looks like:
    | Count | Region | State      |
    | 1     | SBP1   | SendToSales|