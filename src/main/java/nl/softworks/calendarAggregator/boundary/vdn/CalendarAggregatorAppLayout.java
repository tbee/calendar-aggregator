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
import nl.softworks.calendarAggregator.domain.entity.Person;
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

class CalendarAggregatorAppLayout extends AppLayout // https://vaadin.com/docs/latest/components/app-layout
implements HasDynamicTitle {
	private static final Logger LOG = LoggerFactory.getLogger(CalendarAggregatorAppLayout.class);

	@Autowired
	private PlatformTransactionManager transactionManager;

	final protected Tab overviewTab = new Tab(VaadinIcon.TABLE.create(), new Span("Overview") );
	final protected Tab manualTab = new Tab(VaadinIcon.USER.create(), new Span("Manual") );
	final protected Tab scraperTab = new Tab(VaadinIcon.SCISSORS.create(), new Span("Scraper"));
	final protected Tab icalTab = new Tab(VaadinIcon.CALENDAR.create(), new Span("iCal"));
	final protected Tabs tabs = new Tabs(overviewTab, manualTab, scraperTab, icalTab);

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
				UI.getCurrent().navigate(MainView.class);
			}
			if (event.getSelectedTab().equals(manualTab)) {
				UI.getCurrent().navigate(ManualView.class);
			}
//			if (event.getSelectedTab().equals(scraperTab)) {
//				setContent(createPersonContent());
//			}
//			if (event.getSelectedTab().equals(icalTab)) {
//				setContent(createShiftTypeContent());
//			}
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
		CalendarSource calendarSourceManual = new CalendarSource()
				.url("https://www.dansstudiovieberink.nl/kalender.html")
				.name("Dansstudio Vieberink")
				.lat(51.9314535)
				.lon(6.5908473);

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
		showSuccessNotification("Test data added");
	}
}