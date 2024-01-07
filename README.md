# CalendarAggregator
CalendarAggregator, or CA for short, is a website scraping application intended to be used to collect dates from websites and summarize them in one feed.

The original use case for CA is to collect ballroom dancing events from multiple venues.
Each venue has their own website, and their own way of publishing their events.
Finding where at a certain date there were events meant scanning all websites and manually creating a list.
Over and over again. 
That is something computers do better.

## Backend
CA is a spring based application using Vaadin to create a backend.

<img src="readme/backend.png?raw=true" width="1200"/>

In the backend new sources and manual events can be added.
Sources can be unfolded to see the events that currently are present.

A source always contains information about the location of the events it contains.

<img src="readme/manualSource.png?raw=true" width="1000"/>

And optionally more field based on the method used for scraping:
* Manual: this source only contains manual source.
* Regex: use regular expressions to scan for date-time-description patterns
* ICal: read an ical feed and select the relevant vevents.

Future sources may include JSON and/or XML scraping.

## Views
The result is a list of events, that can be viewed as simple HTML:

<img src="readme/html.png?raw=true" width="600"/>

Or can be exported in iCal format to be included in Google calendar.

<img src="readme/ical.png?raw=true" width="600"/>

Both HTML and iCal views can be filtered using a lat/lon position and a as-the-crow-flies distance.

## Getting started

Running `mvn package` will create a fat-jar that can be started using `java -jar`, optionally provide some configuration parameters via -D (like the HTTP ports as Spring has it), and that's it.
Do search the log of the first start for 'note down' to get the password of the automatically created administrator user.

## Technology

The technology stack of CA is intended to be simple and easy - basically allowing a 'MS-Access style' development.
* Spring boot as the basis
* Vaadin for the backend, so no fuss with HTML and browser issues, just plain Java.
* HSQLDB as the database, which is started by CA itself (in server mode on localhost, so it is accessible via standard SQL tools).
* Liquibase for database migration, run automatically when the application starts.

Since the HSQLDB is running in server mode, creating a backup is as simple as connecting to the database and executing `BACKUP DATABASE TO '...' BLOCKING`, and copying the resulting files.

## Architecture

CA uses a simplified DDD / hexagonal / onion architecture:
* There is a domain package which contains entities, aggregates, domain services and what more is known from DDD.
* There is an application package which contains the Vaadin application and rest services (for the views).

Testing is done using functional testing based on Cucumber, in a three layer architecture:
* Functional test scripts (feature files)
* Page Object Model (POM) files
* Glue code (step definitions) to map the test scripts onto the POM files.