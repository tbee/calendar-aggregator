package nl.softworks.calendarAggregator.boundary.vdn;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.server.VaadinSession;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import nl.softworks.calendarAggregator.boundary.SpringUtils;
import nl.softworks.calendarAggregator.domain.ValidationException;
import nl.softworks.calendarAggregator.domain.boundary.R;
import nl.softworks.calendarAggregator.domain.entity.CalendarEvent;
import nl.softworks.calendarAggregator.domain.entity.CalendarSource;
import nl.softworks.calendarAggregator.domain.entity.CalendarSourceRegexScraper;
import nl.softworks.calendarAggregator.domain.entity.Person;
import nl.softworks.calendarAggregator.domain.entity.Timezone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.Callable;

abstract class CalendarAggregatorAppLayout extends AppLayout // https://vaadin.com/docs/latest/components/app-layout
implements HasDynamicTitle {
	private static final Logger LOG = LoggerFactory.getLogger(CalendarAggregatorAppLayout.class);

	@Autowired
	private PlatformTransactionManager transactionManager;

	final protected Tab overviewTab = new Tab(VaadinIcon.TABLE.create(), new Span("Sources") );
	final protected Tab timezoneTab = new Tab(VaadinIcon.CLOCK.create(), new Span("Timezones") );
	final protected Tabs tabs = new Tabs(overviewTab, timezoneTab);

	@Override
	public String getPageTitle() {
		return "Calendar Aggregator";
	}

	public CalendarAggregatorAppLayout(String title) {
		String username = SpringUtils.getLoggedInUsername();

		// Show exceptions as toasts: this is needed to display exceptions thrown by the domain when called through binding from e.g. GridUI
		VaadinSession.getCurrent().setErrorHandler(event -> {
			Throwable t = event.getThrowable();
			Throwable last = t;
			while (t != null) {
				if (t instanceof AlreadyDisplayedException) {
					return;
				}
				if (t instanceof ConstraintViolationException constraintViolationException) {
					for (ConstraintViolation<?> constraintViolation : constraintViolationException.getConstraintViolations()) {
						showErrorNotification(constraintViolation.getPropertyPath() + ": " + constraintViolation.getMessage()); // TODO use text label instead of propertyPath
					}
				}
				last = t;
				t = t.getCause();
			}
			showErrorNotification(last.getMessage());
			if (LOG.isInfoEnabled()) LOG.info(last.getMessage(), event.getThrowable());
		});

		// The drawer toggle icon
		DrawerToggle drawerToggle = new DrawerToggle();
		drawerToggle.setId("drawerToggle");

		// Set the title
		H1 titleH1 = new H1(title);
		titleH1.getStyle()
				.set("font-size", "var(--lumo-font-size-l)")
				.set("margin", "0")
				.set("padding-top", "5px")
				.set("width", title.length() + "em");

		// Menu
		MenuBar menuBar = new MenuBar(); //  https://vaadin.com/directory/component/app-layout-add-on   https://vaadin.com/docs/latest/components/menu-bar
		menuBar.setId("navbar");
		menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY, MenuBarVariant.LUMO_END_ALIGNED);//, MenuBarVariant.LUMO_ICON);
		menuBar.setWidthFull();
		// Admin
		if (userHasRole(Person.Role.ROLE_ADMIN)) {
			menuBar.addItem(createMenuContent("Add data", VaadinIcon.DATABASE), event -> inTransaction(() -> addTestdata()));
		}

		// Tabs
		tabs.setOrientation(Tabs.Orientation.VERTICAL);
		tabs.addSelectedChangeListener(event -> {
			if (event.getSelectedTab().equals(overviewTab)) {
				UI.getCurrent().navigate(CalendarSourceAndEventView.class);
			}
			if (event.getSelectedTab().equals(timezoneTab)) {
				UI.getCurrent().navigate(TimezoneView.class);
			}
		});
		addToDrawer(tabs);

		// Navbar
		addToNavbar(drawerToggle, titleH1, menuBar);
	}

	private HorizontalLayout createMenuContent(String text, VaadinIcon suffixIcon) {
		Icon submenuIcon = new Icon(suffixIcon);
//		submenuIcon.setSize("0.8em");
		HorizontalLayout content = new HorizontalLayout(new NativeLabel(text), submenuIcon);
		content.setMargin(false);
		content.setSpacing(false);
		return content;
	}

	protected boolean userHasRole(Person.Role role) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		boolean hasUserRole = authentication.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals(role.toString()));
		return hasUserRole;
	}

	protected <T> T showException(Callable<T> callable) {
		try {
			return callable.call();
		} catch (Exception e) {
			showException(e);
			throw new AlreadyDisplayedException(e);
		}
	}

	protected void showException(Runnable consumer) {
		try {
			consumer.run();
		} catch (Exception e) {
			showException(e);
		}
	}

	protected void showException(Exception e) {

		// Special handling
		if (e instanceof ValidationException validationException) {
			for (String message : validationException.getMessages()) {
				showErrorNotification(message);
			}
			return;
		}

		// default handling
		LOG.error(e.getMessage(), e);
		showErrorNotification(e.getMessage());
	}

	protected void showErrorNotification(String message) {
		Notification notification = Notification.show(message, 5000, Notification.Position.BOTTOM_CENTER);
		notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
	}

	protected void showSuccessNotification(String message) {
		Notification notification = Notification.show(message, 5000, Notification.Position.BOTTOM_CENTER);
		notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
	}

	public static class AlreadyDisplayedException extends RuntimeException {
		public AlreadyDisplayedException(Exception e) {
			super(e);
		}
	}

	protected void inTransaction(Runnable runnable) {
		new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				runnable.run();
			}
		});
	}
	private void addTestdata() {
		Timezone timezoneEUAMS = new Timezone()
				.name("Europe/Amsterdam")
				.content("""
					BEGIN:DAYLIGHT
					TZOFFSETFROM:+0100
					TZOFFSETTO:+0200
					TZNAME:CEST
					DTSTART:19810329T020000
					RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU
					END:DAYLIGHT
					BEGIN:STANDARD
					TZOFFSETFROM:+0200
					TZOFFSETTO:+0100
					TZNAME:CET
					DTSTART:19961027T030000
					RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU
					END:STANDARD
					""");
		R.timezoneRepo().save(timezoneEUAMS);

		CalendarSource calendarSourceManual = new CalendarSource()
				.url("https://www.dansstudiovieberink.nl/kalender.html")
				.name("Dansstudio Vieberink")
				.location("Bredevoortsestraatweg 121\n7121 BG AALTEN")
				.lat(51.9314535)
				.lon(6.5908473)
				.timezone(timezoneEUAMS);
		{
			LocalDate date = LocalDate.of(2024, 1, 7);
			CalendarEvent calendarEvent = new CalendarEvent()
					.startDateTime(date.atTime(20, 30))
					.endDateTime(date.atTime(22, 30))
					.subject("Workshop Techniek 19.15 uur");
			calendarSourceManual.addCalendarEvent(calendarEvent);
		}
		{
			LocalDate date = LocalDate.of(2024, 1, 21);
			CalendarEvent calendarEvent = new CalendarEvent()
					.startDateTime(date.atTime(20, 30))
					.endDateTime(date.atTime(22, 30))
					.subject("Workshop Techniek 19.15 uur");
			calendarSourceManual.addCalendarEvent(calendarEvent);
		}
		R.calendarSource().save(calendarSourceManual);

		CalendarSource cityDance = new CalendarSourceRegexScraper()
				.regex("([a-zA-Z]*) +[a-z]{2}\\. ([0-9][0-9]? +(januari|februari|maart|april|mei|juni|juli|augustus|september|oktober|november|december) +[0-9]{4}) +van ([0-9]+:[0-9]+) tot ([0-9]+:[0-9]+)")
				.subjectGroupIdx(1)
				.startDateGroupIdx(2)
				.endDateGroupIdx(2)
				.datePattern("d MMMM yyyy")
				.startTimeGroupIdx(4)
				.endTimeGroupIdx(5)
				.timePattern("HH:mm")
				.dateTimeLocale("NL")
				.scrapeUrl("https://mijn.citydance.nl/agenda")
				.url("https://www.citydance.nl/")
				.name("City Dance")
				.location("Varsseveldseweg 89\n7002 LJ Doetinchem")
				.lat(51.9666992)
				.lon(6.3034428)
				.timezone(timezoneEUAMS);
//		cityDance.generateEvents(null);
		R.calendarSource().save(cityDance);

		CalendarSource danssalonNieuwendijk = new CalendarSourceRegexScraper()
				.regex("([0-9][0-9]? +(januari|februari|maart|april|mei|juni|juli|augustus|september|oktober|november|december) +[0-9]{4})")
				.startDateGroupIdx(1)
				.endDateGroupIdx(1)
				.datePattern("d MMMM yyyy")
				.startTimeDefault("14:30")
				.endTimeDefault("18:00")
				.timePattern("HH:mm")
				.dateTimeLocale("NL")
				.scrapeUrl("https://de-danssalon.nl/agenda/")
				.scrapeBlockStart("Locatie Nieuwendijk")
				.scrapeBlockEnd("Entree:")
				.url("https://de-danssalon.nl/agenda/")
				.name("Danssalon in Nieuwendijk")
				.location("H.F. Witte Centrum\nHenri Dunantplein 4\n3731 CL, De Bilt")
				.lat(51.9314535)
				.lon(6.5908473)
				.timezone(timezoneEUAMS);
//		danssalonNieuwendijk.generateEvents(null);
		R.calendarSource().save(danssalonNieuwendijk);

		CalendarSource danssalonDeBilt = new CalendarSourceRegexScraper()
				.regex("([0-9][0-9]? +(januari|februari|maart|april|mei|juni|juli|augustus|september|oktober|november|december) +[0-9]{4})")
				.startDateGroupIdx(1)
				.endDateGroupIdx(1)
				.datePattern("d MMMM yyyy")
				.startTimeDefault("14:30")
				.endTimeDefault("18:00")
				.timePattern("HH:mm")
				.dateTimeLocale("NL")
				.scrapeUrl("https://de-danssalon.nl/agenda/")
				.scrapeBlockStart("Locatie de Bilt")
				.scrapeBlockEnd("Entree:")
				.url("https://de-danssalon.nl/agenda/")
				.name("Danssalon in de Bilt")
				.location("Dorpshuis Tavenu\nSingel 16a\n4255 HD, Nieuwendijk")
				.lat(51.9314535)
				.lon(6.5908473)
				.timezone(timezoneEUAMS);
//		danssalonDeBilt.generateEvents(null);
		R.calendarSource().save(danssalonDeBilt);

		CalendarSource styledancing2023 = new CalendarSourceRegexScraper()
				.regex("([0-9][0-9]? +(januari|februari|maart|april|mei|juni|juli|augustus|september|oktober|november|december))")
				.startDateGroupIdx(1)
				.endDateGroupIdx(1)
				.datePattern("d MMMM")
				.yearDefault(2023)
				.startTimeDefault("20:30")
				.endTimeDefault("23:30")
				.timePattern("HH:mm")
				.dateTimeLocale("NL")
				.scrapeUrl("https://www.styledancing.nl/agenda-dansavond/")
				.scrapeBlockStart("Data in 2023")
				.scrapeBlockEnd("Data in")
				.url("https://www.styledancing.nl/agenda-dansavond/")
				.name("Styledancing 2023")
				.location("De Kentering, Dorpsstraat 54, 5241 ED Rosmalen")
				.lat(51.7157652)
				.lon(5.3607253)
				.timezone(timezoneEUAMS);
//		styledancing2023.generateEvents(null);
		R.calendarSource().save(styledancing2023);

		CalendarSource styledancing2024 = new CalendarSourceRegexScraper()
				.regex("([0-9][0-9]? +(januari|februari|maart|april|mei|juni|juli|augustus|september|oktober|november|december))")
				.startDateGroupIdx(1)
				.endDateGroupIdx(1)
				.datePattern("d MMMM")
				.yearDefault(2024)
				.startTimeDefault("20:30")
				.endTimeDefault("23:30")
				.timePattern("HH:mm")
				.dateTimeLocale("NL")
				.scrapeUrl("https://www.styledancing.nl/agenda-dansavond/")
				.scrapeBlockStart("Data in 2024")
				//.scrapeBlockEnd("Data in")
				.url("https://www.styledancing.nl/agenda-dansavond/")
				.name("Styledancing 2024")
				.location("De Kentering, Dorpsstraat 54, 5241 ED Rosmalen")
				.lat(51.7157652)
				.lon(5.3607253)
				.timezone(timezoneEUAMS);
//		styledancing2024.generateEvents(null);
		R.calendarSource().save(styledancing2024);

		showSuccessNotification("Data added");
	}
}