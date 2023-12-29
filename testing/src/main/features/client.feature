Feature: Client tests

  Background:
    Given user "administrator" is logged in

  Scenario: create client
    When client is added
      | Client id | BSN       | Initials | First name | Birth name  | Birth date |
      | 11        | 123456782 | D.       | Donald     | Duck        | 09-06-1934 |
    Then client "11" should exist

  Scenario: add client address
    Given client exist
      | Client id | Initials | First name | Birth name  |
      | 6667      | K        | Karel      | Paardepoot  |
    When client-address is navigated to
    And client-address is added
      | Street     | Number  | Suffix | ZIP    | City     |
      | Kwakstraat | 2       | a      | 1111AA | Duckstad |
    Then client-address should exist
      | Street     | Number  | Suffix | ZIP    | City     |
      | Kwakstraat | 2       | a      | 1111AA | Duckstad |

  Scenario: search and select client
    Given client exist
      | Client id | Initials | First name | Birth name  |
      | 6667      | K        | Karel      | Paardepoot  |
    When client-search is navigated to
    And client-search searches for "Paardepoot"
    And client-search selects client "6667"
    Then client-overview should be visible
    And client-overview subject "Locaties" should contain "geen locaties"
