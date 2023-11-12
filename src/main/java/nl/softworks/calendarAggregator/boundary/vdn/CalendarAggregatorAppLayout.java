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

import java.time.LocalDateTime;
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
			while (t.getCause() != null) {
				if (t instanceof AlreadyDisplayedException) {
					return;
				}
				t = t.getCause();
			}
			showErrorNotification(t.getMessage());
			if (LOG.isInfoEnabled()) LOG.info(t.getMessage(), event.getThrowable());
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
			menuBar.addItem(createMenuContent("Add test data", VaadinIcon.DATABASE), event -> inTransaction(() -> addTestdata()));
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
				.name("Manual")
				.lat(51.9314535)
				.lon(6.5908473)
				.timezone(timezoneEUAMS);
		{
			CalendarEvent calendarEvent = new CalendarEvent()
					.startDateTime(LocalDateTime.now())
					.endDateTime(LocalDateTime.now().plusHours(3))
					.subject("dancing");
			calendarSourceManual.addCalendarEvent(calendarEvent);
		}
		{
			CalendarEvent calendarEvent = new CalendarEvent()
					.startDateTime(LocalDateTime.now().plusDays(1))
					.endDateTime(LocalDateTime.now().plusDays(1).plusHours(3))
					.subject("dancing2");
			calendarSourceManual.addCalendarEvent(calendarEvent);
		}
		R.calendarSource().save(calendarSourceManual);

		CalendarSource calendarSourceRegexScraper = new CalendarSourceRegexScraper()
				.content("""
						DANSAVOND
						 za. 18 november 2023
						 van 20:00 tot 23:59 uur
						Voor alle ballroom- & Latin dansers is er op zaterdag weer een gezellige dansavond bij Citydance!
						Zien we je daar?
						      
						Tijd: 20.00 - 00.00
						Entree: € 7,50,- per persoon
						      
						Heb je danservaring maar ben je geen lid?
						Of heb je helemaal geen danservaring maar wil je wel gewoon gezellig langskomen?
						Je bent natuurlijk van harte welkom!
						Stuur ons gerust een mailtje.
						      
						Pietendansfeest
						 za. 25 november 2023
						 van 10:30 tot 12:00 uur
						Op zaterdag 25 november nodigen wij daarom de kleinste kinderen graag uit voor ons Pieten Dansfeest! (3 tot en met 8 jaar)
						We starten om 10.30 uur op locatie Citydance (Varsseveldseweg 89, Doetinchem)
						Wanneer alle dansjes goed gedanst zijn komen misschien zelfs de pieten wel om de kinderen te verblijden met een kadootje en natuurlijk niet te vergeten, pepernoten!
						      
						Rond 12.00 uur zullen de pieten weer op doorreis gaan en zwaaien we iedereen uit!
						Kosten € 10,00 per kind (contant te voldoen) op 25 november aan de deur.
						Uiteraard zijn vriendjes/vriendinnetjes ook van harte welkom!
						      
						Aanmelden VOOR 23 november (voor leden via de Citydance app en niet leden via mail aanmelden) zodat de Piet weet hoeveel kadootjes hij mee moet nemen.
						Vermeld bij aanmelding de naam en leeftijd en ook graag van eventuele vriendjes/vriendinnetjes
						Mailen naar:  info@citydance.nl
						      
						Kerstgala
						 za. 16 december 2023
						 van 20:30 tot 23:59 uur
						16 December het Kerstgala !!
						      
						We kijken er alweer heel erg naar uit.
						      
						Meld je aan via je leden app of stuur een mailtje naar info@citydance.nl
						Natuurlijk zijn introducees en niet-leden ook van harte welkom.
						      
						Laat je verrassen door mooie optredens tijdens de avond, wij zorgen voor de hapjes en uiteraard zal een lekker welkomstdrankje niet ontbreken.
						      
						Dresscode; Gala
						      
						Betaling contant te voldoen bij aanvang € 22,50
						      
						Deuren zijn open vanaf 20.00 uur\s
						""")
				.regex("([a-zA-Z]*) +[a-z]{2}\\. ([0-9][0-9]? +(januari|februari|maart|april|mei|juni|juli|augustus|september|oktober|november|december) +[0-9]{4}) +van ([0-9]+:[0-9]+) tot ([0-9]+:[0-9]+)")
				.subjectGroupIdx(1)
				.startDateGroupIdx(2)
				.endDateGroupIdx(2)
				.datePattern("dd MMMM yyyy")
				.startTimeGroupIdx(4)
				.endTimeGroupIdx(5)
				.timePattern("HH:mm")
				.dateTimeLocale("NL")
				//
				.url("https://www.dansstudiovieberink.nl/kalender.html")
				.name("Dansstudio Vieberink")
				.lat(51.9314535)
				.lon(6.5908473)
				.timezone(timezoneEUAMS);
		calendarSourceRegexScraper.generateEvents(null);
		R.calendarSource().save(calendarSourceRegexScraper);

		showSuccessNotification("Test data added");
	}
}