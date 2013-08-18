Feature: Submit an RFQ as a user with sales in ten regions

  Background:
    Propagation of an RFQ between two regions with sale people connected

  Scenario: RFQ is submitted and user gets a quote
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
    | Sales2  | Quote   |
    Then the FSM looks like:
    | Count | Region | State | Filler |
    | 1     | SBP1   | Pickup| Sales1 |
    | 1     | SBP2   | Pickup| Sales2 |
    | 1     | SBP3   | Pickup| Sales3 |
    | 1     | SBP4   | Pickup| Sales4 |
    | 1     | SBP5   | Pickup| Sales5 |
    | 1     | SBP6   | Pickup| Sales6 |
    | 1     | SBP7   | Pickup| Sales7 |
    | 1     | SBP8   | Pickup| Sales8 |
    | 1     | SBP9   | Pickup| Sales9 |
    | 1     | SBP10  | Pickup| Sales10|
    | 1     | SBP2   | Quote | Sales2 |

  Scenario: RFQ is submitted and user gets a quote
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
    | Count | Region | State | Filler |
    | 1     | SBP1   | Pickup| Sales1 |
    | 1     | SBP2   | Pickup| Sales2 |
    | 1     | SBP3   | Pickup| Sales3 |
    | 0     | SBP4   | Pickup| Sales4 |
    | 1     | SBP5   | Pickup| Sales5 |
    | 1     | SBP6   | Pickup| Sales6 |
    | 1     | SBP7   | Pickup| Sales7 |
    | 0     | SBP8   | Pickup| Sales8 |
    | 1     | SBP9   | Pickup| Sales9 |
    | 1     | SBP10  | Pickup| Sales10|
    | 1     | SBP2   | Quote | Sales2 |

  Scenario: RFQ iby one of two users who gets a quote after multiple putbacks from the sales
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
    | Count | Region | State | Filler |
    | 2     | SBP1   | Pickup| Sales1 |
    | 2     | SBP2   | Pickup| Sales2 |
    | 2     | SBP3   | Pickup| Sales3 |
    | 2     | SBP4   | Pickup| Sales4 |
    | 2     | SBP5   | Pickup| Sales5 |
    | 2     | SBP6   | Pickup| Sales6 |
    | 2     | SBP7   | Pickup| Sales7 |
    | 0     | SBP8   | Pickup| Sales8 |
    | 2     | SBP9   | Pickup| Sales9 |
    | 2     | SBP10  | Pickup| Sales10|
    | 1     | SBP2   | Quote | Sales2 |
@focus
  Scenario: RFQ is submitted by two user who gets quotes after multiple putbacks from the sales
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
    | User2   | StartRFQ|
    | Sales1  | Putback |
    | Sales2  | Putback |
    | Sales3  | Putback |
    | Sales4  | Putback |
    | Sales5  | Putback |
    | Sales4  | Quote   |
    | Sales6  | Putback |
    | Sales7  | Putback |
    | Sales9  | Putback |
    | Sales10 | Putback |
    | Sales2  | Quote   |
    Then the FSM looks like:
    | Count | Region | State | Filler |
    | 2     | SBP1   | Pickup| Sales1 |
    | 2     | SBP2   | Pickup| Sales2 |
    | 2     | SBP3   | Pickup| Sales3 |
    | 2     | SBP4   | Pickup| Sales4 |
    | 2     | SBP5   | Pickup| Sales5 |
    | 2     | SBP6   | Pickup| Sales6 |
    | 2     | SBP7   | Pickup| Sales7 |
    | 0     | SBP8   | Pickup| Sales8 |
    | 2     | SBP9   | Pickup| Sales9 |
    | 2     | SBP10  | Pickup| Sales10|
    | 1     | SBP2   | Quote | Sales2 |
    | 1     | SBP4   | Quote | Sales4 |
