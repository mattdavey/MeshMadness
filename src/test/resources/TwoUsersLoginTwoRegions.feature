Feature: Interaction of an RFQ between user and salesperson who are in two different regions

  Background:
    Propagation of an RFQ between two regions

  Scenario: A user connected to a region and submits an RFQ
    Given the following users are logged in
    | Role   | Region |
    | User1  | SBP1   |
    When users submit messages as follows
    | Role  | Region |
    | User1 | SBP1   |
    Then the FSM looks like:
    | Count | Region |
    | 1     | SBP1   |

  Scenario: Two user's connected to a region and submits RFQ's
    Given the following users are logged in
    | Role   | Region |
    | User1  | SBP1   |
    | User2  | SBP1   |
    When users submit messages as follows
    | Role  | Region |
    | User1 | SBP1   |
    | User2 | SBP1   |
    Then the FSM looks like:
    | Count | Region |
    | 2     | SBP1   |

  Scenario: Two user's connected to separate region and submits RFQ's
    Given the following users are logged in
    | Role   | Region |
    | User1  | SBP1   |
    | User2  | SBP2   |
    When users submit messages as follows
    | Role  | Region |
    | User1 | SBP1   |
    | User2 | SBP2   |
    Then the FSM looks like:
    | Count | Region |
    | 2     | SBP1   |
    | 2     | SBP2   |
