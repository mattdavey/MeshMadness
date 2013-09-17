Feature: Submit an RFQ by users with no sales interaction

  Background:
    Propagation of an RFQ between two regions with no sale people connected

  Scenario: A user connected to a region and submits an RFQ
    Given the following users are logged in
    | Role   | Region |
    | User1  | SBP1   |
    When users submit messages as follows
    | Role  | Message | Id    |
    | User1 | StartRFQ|  1    |
    Then the FSM looks like:
    | Count | Region | Id | State    |
    | 1     | SBP1   | 1  | SendToDI |


  Scenario: Two user's connected to a region and submits RFQ's
    Given the following users are logged in
    | Role   | Region |
    | User1  | SBP1   |
    | User2  | SBP1   |
    When users submit messages as follows
    | Role  | Message | Id    |
    | User1 | StartRFQ|  1    |
    | User2 | StartRFQ|  2    |
    Then the FSM looks like:
    | Count | Region | Id    | State    |
    | 1     | SBP1   |  1    | SendToDI |
    | 1     | SBP1   |  2    | SendToDI |


  Scenario: Two user's connected to separate region and submits RFQ's
    Given the following users are logged in
    | Role   | Region |
    | User1  | SBP1   |
    | User2  | SBP2   |
    When users submit messages as follows
    | Role  | Message | Id |
    | User1 | StartRFQ|  1 |
    | User2 | StartRFQ|  2 |
    Then the FSM looks like:
    | Count | Region | Id | State    |
    | 1     | SBP1   | 1  | SendToDI |
    | 1     | SBP2   | 2  | SendToDI |
