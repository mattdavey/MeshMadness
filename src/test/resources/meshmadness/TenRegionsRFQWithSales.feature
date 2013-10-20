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
    | Role    | Message | Id |
    | User1   | StartRFQ|  1 |
    | Sales1  | Putback |  1 |
    | Sales2  | Putback |  1 |
    | Sales3  | Putback |  1 |
    | Sales4  | Putback |  1 |
    | Sales5  | Putback |  1 |
    | Sales6  | Putback |  1 |
    | Sales7  | Putback |  1 |
    | Sales8  | Putback |  1 |
    | Sales9  | Putback |  1 |
    | Sales10 | Putback |  1 |
    | Sales2  | Quote   |  1 |
    Then the FSM looks like:
    | Count | Region | Id | State | Filler |
    | 1     | SBP1   | 1  | Pickup|        |
    | 1     | SBP2   | 1  | Pickup|        |
    | 1     | SBP3   | 1  | Pickup|        |
    | 1     | SBP4   | 1  | Pickup|        |
    | 1     | SBP5   | 1  | Pickup|        |
    | 1     | SBP6   | 1  | Pickup|        |
    | 1     | SBP7   | 1  | Pickup|        |
    | 1     | SBP8   | 1  | Pickup|        |
    | 1     | SBP9   | 1  | Pickup|        |
    | 1     | SBP10  | 1  | Pickup|        |
    | 1     | SBP2   | 1  | Quote | Sales2 |

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
    | Role    | Message | Id |
    | User1   | StartRFQ| 1  |
    | Sales1  | Putback | 1  |
    | Sales2  | Putback | 1  |
    | Sales3  | Putback | 1  |
    | Sales5  | Putback | 1  |
    | Sales6  | Putback | 1  |
    | Sales7  | Putback | 1  |
    | Sales9  | Putback | 1  |
    | Sales10 | Putback | 1  |
    | Sales2  | Quote   | 1  |
    Then the FSM looks like:
    | Count | Region | Id | State | Filler |
    | 1     | SBP1   | 1  | Pickup|        |
    | 1     | SBP2   | 1  | Pickup|        |
    | 1     | SBP3   | 1  | Pickup|        |
    | 0     | SBP4   | 1  | Pickup|        |
    | 1     | SBP5   | 1  | Pickup|        |
    | 1     | SBP6   | 1  | Pickup|        |
    | 1     | SBP7   | 1  | Pickup|        |
    | 0     | SBP8   | 1  | Pickup|        |
    | 1     | SBP9   | 1  | Pickup|        |
    | 1     | SBP10  | 1  | Pickup|        |
    | 1     | SBP2   | 1  | Quote | Sales2 |

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
    | Role    | Message | Id |
    | User1   | StartRFQ| 1  |
    | Sales1  | Putback | 1  |
    | Sales2  | Putback | 1  |
    | Sales3  | Putback | 1  |
    | Sales4  | Putback | 1  |
    | Sales5  | Putback | 1  |
    | Sales6  | Putback | 1  |
    | Sales7  | Putback | 1  |
    | Sales9  | Putback | 1  |
    | Sales10 | Putback | 1  |
    | Sales1  | Putback | 1  |
    | Sales2  | Putback | 1  |
    | Sales3  | Putback | 1  |
    | Sales4  | Putback | 1  |
    | Sales5  | Putback | 1  |
    | Sales6  | Putback | 1  |
    | Sales7  | Putback | 1  |
    | Sales9  | Putback | 1  |
    | Sales10 | Putback | 1  |
    | Sales2  | Quote   | 1  |
    Then the FSM looks like:
    | Count | Region | Id | State | Filler |
    | 2     | SBP1   | 1  | Pickup|        |
    | 2     | SBP2   | 1  | Pickup|        |
    | 2     | SBP3   | 1  | Pickup|        |
    | 2     | SBP4   | 1  | Pickup|        |
    | 2     | SBP5   | 1  | Pickup|        |
    | 2     | SBP6   | 1  | Pickup|        |
    | 2     | SBP7   | 1  | Pickup|        |
    | 0     | SBP8   | 1  | Pickup|        |
    | 2     | SBP9   | 1  | Pickup|        |
    | 2     | SBP10  | 1  | Pickup|        |
    | 1     | SBP2   | 1  | Quote | Sales2 |

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
    | Role    | Message | Id |
    | User1   | StartRFQ|  1 |
    | Sales1  | Putback |  1 |
    | Sales2  | Putback |  1 |
    | Sales3  | Putback |  1 |
    | Sales4  | Putback |  1 |
    | Sales5  | Putback |  1 |
    | Sales6  | Putback |  1 |
    | Sales7  | Putback |  1 |
    | Sales9  | Putback |  1 |
    | Sales10 | Putback |  1 |
    | User2   | StartRFQ|  2 |
    | Sales1  | Putback |  1 |
    | Sales2  | Putback |  1 |
    | Sales3  | Putback |  1 |
    | Sales4  | Putback |  1 |
    | Sales5  | Putback |  1 |
    | Sales4  | Quote   |  1 |
    | Sales6  | Putback |  1 |
    | Sales7  | Putback |  1 |
    | Sales9  | Putback |  1 |
    | Sales10 | Putback |  1 |
    | Sales2  | Quote   |  2 |
    Then the FSM looks like:
    | Count | Region | Id | State | Filler |
    | 2     | SBP1   | 1  | Pickup|        |
    | 2     | SBP2   | 1  | Pickup|        |
    | 2     | SBP3   | 1  | Pickup|        |
    | 2     | SBP4   | 1  | Pickup|        |
    | 2     | SBP5   | 1  | Pickup|        |
    | 2     | SBP6   | 1  | Pickup|        |
    | 2     | SBP7   | 1  | Pickup|        |
    | 0     | SBP8   | 1  | Pickup|        |
    | 2     | SBP9   | 1  | Pickup|        |
    | 2     | SBP10  | 1  | Pickup|        |
    | 1     | SBP2   | 2  | Quote | Sales2 |
    | 1     | SBP4   | 1  | Quote | Sales4 |
