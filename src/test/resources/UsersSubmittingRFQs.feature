Feature: Submit an RFQ by users

  Background:
    Propagation of an RFQ between two regions with no sale people connected

  Scenario: A user connected to a region and submits an RFQ
    Given the following users are logged in
    | Role   | Region | Dialog |
    | User1  | SBP1   |        |
    When users submit messages as follows
    | Role  | Region |
    | User1 | SBP1   |
    Then the FSM looks like:
    | Count | Region | State       |
    | 1     | SBP1   | SendToSales |

  Scenario: Two user's connected to a region and submits RFQ's
    Given the following users are logged in
    | Role   | Region | Dialog |
    | User1  | SBP1   |        |
    | User2  | SBP1   |        |
    When users submit messages as follows
    | Role  | Region |
    | User1 | SBP1   |
    | User2 | SBP1   |
    Then the FSM looks like:
    | Count | Region | State       |
    | 2     | SBP1   | SendToSales |

  Scenario: Two user's connected to separate region and submits RFQ's
    Given the following users are logged in
    | Role   | Region | Dialog |
    | User1  | SBP1   |        |
    | User2  | SBP2   |        |
    When users submit messages as follows
    | Role  | Region |
    | User1 | SBP1   |
    | User2 | SBP2   |
    Then the FSM looks like:
    | Count | Region | State       |
    | 2     | SBP1   | SendToSales |
    | 2     | SBP2   | SendToSales |
