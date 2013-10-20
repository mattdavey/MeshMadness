Feature: Submit an RFQ to a single region with one sales actor

  Scenario: A user connected to a region and submits an RFQ
    Given the following users are logged in
      | Role   | Region |
      | User1  | SBP1   |
      | Sales1 | SBP1   |
    When users submit messages as follows
      | Role   | Message  | Id |
      | User1  | StartRFQ | 1  |
      | Sales1 | Putback  | 1  |
      | Sales1 | Putback  | 1  |
      | Sales1 | Quote    | 1  |
    Then the FSM looks like:
      | Count | Region | Id | State | Filler |
      | 1     | SBP1   | 1  | Quote | Sales1 |
