# Non negotiable rules
- Never ever perform any git actions: committing, pushing deleting is done by the user.

# Functionality
This is an application that scrawls websites or ical feeds in search for events.
It then creates records for each found event, so it can be aggregated into a month calendar, list view or ical feed.
The scan logic runs once per day.
The application automatically removes events that are in the past.

- The application defines locations (CalendarLocation) that defines a based url, an address and lat and lon coordinates.
- Each location has one or more sources (CalendarSource), that define a method of crawling, like http+regex, ical+regex, or AI.
- The result of each scawl are CalendarEvents, actual events with a time and location.

# Architecture
The application uses a light form of DDD (Domain driver design) with a domain and infrastructure layer.
An application layer did not make much sense yet, but that may change in the future.

## web
The web layer consists of three parts:
- Two thymeleaf based pages, that render the month calendar view and list view in the root (/) rendered via the CalendarController.
- A Vaadin application that is used for maintaining the domain entities under /app. 

The Vaadin application requires a login, the thymeleaf pages are public.
There also is the ICalResource that generates the ical feed.
The CalendarResource publishes all locations for future use.