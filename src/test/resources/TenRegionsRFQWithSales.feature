Feature: Submit an RFQ as a user with sales in ten regions

  Background:
    Propagation of an RFQ between two regions with sale people connected

  Scenario: A user connected to one of ten region.  All regions have a sales connected. RFQ is submitted and user gets a quote
    Given the following users are logged in
    | Role    | Region |
    | User1   | SBP1   |
    | Sales1  | SBP1   |
    | Sales2  | SBP2   |
    | Sales3  | SBP3   |
    | Sales4  | SBP4   |
    | Sales5  | SBP5   |
    | Sales6  | SBP6   |
    | Sales7  | SBP7   |
    | Sales8  | SBP8   |
    | Sales9  | SBP9   |
    | Sales10 | SBP10  |
    When users submit messages as follows
    | Role    | Message |
    | User1   | StartRFQ|
    | Sales1  | Putback |
    | Sales2  | Putback |
    | Sales3  | Putback |
    | Sales4  | Putback |
    | Sales5  | Putback |
    | Sales6  | Putback |
    | Sales7  | Putback |
    | Sales8  | Putback |
    | Sales9  | Putback |
    | Sales10 | Putback |
    | Sales2| Quote   |
    Then the FSM looks like:
    | Count | Region | State | Filler|
    | 1     | SBP2   | Quote | Sales2|

  Scenario: A user connected to one of ten region.  Some regions have a sales connected. RFQ is submitted and user gets a quote
    Given the following users are logged in
    | Role    | Region |
    | User1   | SBP1   |
    | Sales1  | SBP1   |
    | Sales2  | SBP2   |
    | Sales3  | SBP3   |
    | User2   | SBP4   |
    | Sales5  | SBP5   |
    | Sales6  | SBP6   |
    | Sales7  | SBP7   |
    | User3   | SBP8   |
    | Sales9  | SBP9   |
    | Sales10 | SBP10  |
    When users submit messages as follows
    | Role    | Message |
    | User1   | StartRFQ|
    | Sales1  | Putback |
    | Sales2  | Putback |
    | Sales3  | Putback |
    | Sales5  | Putback |
    | Sales6  | Putback |
    | Sales7  | Putback |
    | Sales9  | Putback |
    | Sales10 | Putback |
    | Sales2  | Quote   |
    Then the FSM looks like:
    | Count | Region | State | Filler|
    | 1     | SBP2   | Quote | Sales2|

  Scenario: A user connected to one of ten region.  Some regions have a sales connected. RFQ is submitted and user gets a quote after multiple attempts
    Given the following users are logged in
    | Role    | Region |
    | User1   | SBP1   |
    | Sales1  | SBP1   |
    | Sales2  | SBP2   |
    | Sales3  | SBP3   |
    | User2   | SBP4   |
    | Sales4  | SBP4   |
    | Sales5  | SBP5   |
    | Sales6  | SBP6   |
    | Sales7  | SBP7   |
    | Sales8  | SBP8   |
    | Sales9  | SBP9   |
    | Sales10 | SBP10  |
    When users submit messages as follows
    | Role    | Message |
    | User1   | StartRFQ|
    | Sales1  | Putback |
    | Sales2  | Putback |
    | Sales3  | Putback |
    | Sales4  | Putback |
    | Sales5  | Putback |
    | Sales6  | Putback |
    | Sales7  | Putback |
    | Sales9  | Putback |
    | Sales10 | Putback |
    | Sales1  | Putback |
    | Sales2  | Putback |
    | Sales3  | Putback |
    | Sales4  | Putback |
    | Sales5  | Putback |
    | Sales6  | Putback |
    | Sales7  | Putback |
    | Sales9  | Putback |
    | Sales10 | Putback |
    | Sales2  | Quote   |
    Then the FSM looks like:
    | Count | Region | State | Filler|
    | 1     | SBP2   | Quote | Sales2|

