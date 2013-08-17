Feature: Submit an RFQ to a single region with one sales actor

  Scenario: A user connected to a region and submits an RFQ
    Given the following users are logged in
    | Role   | Region |
    | User1  | SBP1   |
    | Sales1 | SBP1   |
    When users submit messages as follows
    | Role  | Message |
    | User1 | StartRFQ|
    | Sales1| Putback |
    | Sales1| Putback |
    | Sales1| Quote   |
    Then the FSM looks like:
    | Count | Region | State | Filler |
    | 1     | SBP1   | Quote | Sales1 |
