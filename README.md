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

<img src="readme/list.png?raw=true" width="600"/>

Or in a week calendar style:

<img src="readme/month.png?raw=true" width="600"/>

Or can be exported in iCal format to be included in Google calendar.

<img src="readme/ical.png?raw=true" width="600"/>

Both HTML and iCal views can be filtered using a lat/lon position and a as-the-crow-flies distance.

## Getting started

To run locally create an application-default.properties file in the `app/src/main/resources` folder and set the database credentials `spring.datasource.password`.

Running `mvn package` will create a bootable-jar that can be started using `java -jar`, optionally provide some configuration parameters via -D (like the HTTP ports as Spring has it), and that's it.
Do search the log of the first start for 'note down' to get the password of the automatically created administrator user.

## AI agent scraper
The `AIAgent` source type sends a system and user prompt to an LLM and lets the model drive a
real browser to find the events. The browser is a separate
[Playwright MCP](https://github.com/microsoft/playwright-mcp) server; the application connects to
it as an MCP client.

In development `CalendarAggregatorApplicationTestContainer` starts the server with Testcontainers
and passes its mapped port to the application. To run the server by hand:

```
docker run --rm -it --init --ipc=host -p 8931:8931 \
  mcr.microsoft.com/playwright/mcp:v0.0.83 \
  --isolated --port 8931 --host 0.0.0.0 --allowed-hosts '*' --snapshot-mode none
```

Configure the url with `calendaraggregator.mcp.playwright.url` (default `http://localhost:8931`).
In production the value comes from `CALENDARAGGREGATOR_MCP_PLAYWRIGHT_URL`.

Two server flags matter:
* `--isolated` gives every MCP client its own browser context, so concurrent scrapes do not
  share a page. Without it, a second client fails because the browser profile is already in use.
* `--allowed-hosts` must contain the `Host` header the application sends, e.g.
  `playwright:8931`. The default only allows localhost, so a container-to-container connection
  gets a 403.
* `--snapshot-mode none` stops Playwright MCP from writing snapshots to files; the agent reads a
  page with `browser_snapshot` instead, which returns the accessibility tree inline.

The model must support tool calling; the AIAgent form only lists models that advertise it
(OpenRouter's `supported_parameters` contains `tools`). Defaults to DeepSeek.

### Flyer images
Some venues publish their events only as a poster image. During a run the agent can call the
`inspectFlyers` tool. That tool scrolls the rendered page, collects the large `<img>` elements,
downloads them (plain HTTP first, the browser itself as fallback) and sends them to a vision model.
It extracts the events and adds them to the source directly.

* `calendaraggregator.aiAgent.visionModel` selects the vision model (default
  `deepseek/deepseek-v4-flash-vision-exp`). It must accept image input.
* Candidates come from rendered `<img>` elements, links to image files, and the page's resource
  timeline (which covers CSS background images). Each candidate is size-checked from the DOM and,
  more reliably, from the downloaded bytes (`flyerMinWidth` × `flyerMinHeight`, default 400 × 300).
* Downloaded images are restricted to the seed hosts plus hosts the page actually contacted, and
  never to loopback or private addresses.
* Extractions are cached in `flyer_cache`, keyed by image hash, prompt version and model, so an
  unchanged flyer is not sent to the vision model on every daily run. Rows not seen for
  `flyerCacheRetentionDays` (default 90) whose events have also passed are removed automatically.

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