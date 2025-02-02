package nl.softworks.calendarAggregator.application.vdn;

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
import nl.softworks.calendarAggregator.application.vdn.view.CalendarLocationAndSourceView;
import nl.softworks.calendarAggregator.application.vdn.view.LabelGroupView;
import nl.softworks.calendarAggregator.application.vdn.view.LabelView;
import nl.softworks.calendarAggregator.application.vdn.view.PersonView;
import nl.softworks.calendarAggregator.application.vdn.view.SettingsView;
import nl.softworks.calendarAggregator.application.vdn.view.TimezoneView;
import nl.softworks.calendarAggregator.domain.ValidationException;
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

import java.util.concurrent.Callable;

public abstract class CalendarAggregatorAppLayout extends AppLayout // https://vaadin.com/docs/latest/components/app-layout
implements HasDynamicTitle {
	private static final Logger LOGGER = LoggerFactory.getLogger(CalendarAggregatorAppLayout.class);

	@Autowired
	private PlatformTransactionManager transactionManager;

	final protected String title;
	final protected Tab overviewTab = new Tab(VaadinIcon.TABLE.create(), new Span("Sources") );
	final protected Tab timezoneTab = new Tab(VaadinIcon.CLOCK.create(), new Span("Timezones") );
	final protected Tab labelGroupTab = new Tab(VaadinIcon.GROUP.create(), new Span("Label groups") );
	final protected Tab labelTab = new Tab(VaadinIcon.TEXT_LABEL.create(), new Span("Labels") );
	final protected Tab personTab = new Tab(VaadinIcon.USER.create(), new Span("Persons") );
	final protected Tab settingsTab = new Tab(VaadinIcon.LIST.create(), new Span("Settings") );
	final protected Tabs tabs = new Tabs(overviewTab, timezoneTab, labelGroupTab, labelTab, personTab, settingsTab);

	@Override
	public String getPageTitle() {
		return "Calendar Aggregator";
	}

	public CalendarAggregatorAppLayout(String title) {
		this.title = title;
		//String username = SpringUtils.getLoggedInUsername();

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
			 if (LOGGER.isInfoEnabled())  LOGGER.info(last.getMessage(), event.getThrowable());
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
//		if (userHasRole(Person.Role.ROLE_ADMIN)) {
//			menuBar.addItem(createMenuContent("Add data", VaadinIcon.DATABASE), event -> inTransaction(() -> addTestdata()));
//		}

		// Tabs
		tabs.setOrientation(Tabs.Orientation.VERTICAL);
		tabs.addSelectedChangeListener(event -> {
			if (event.getSelectedTab().equals(overviewTab)) {
				UI.getCurrent().navigate(CalendarLocationAndSourceView.class);
			}
			if (event.getSelectedTab().equals(timezoneTab)) {
				UI.getCurrent().navigate(TimezoneView.class);
			}
			if (event.getSelectedTab().equals(labelGroupTab)) {
				UI.getCurrent().navigate(LabelGroupView.class);
			}
			if (event.getSelectedTab().equals(labelTab)) {
				UI.getCurrent().navigate(LabelView.class);
			}
			if (event.getSelectedTab().equals(personTab)) {
				UI.getCurrent().navigate(PersonView.class);
			}
			if (event.getSelectedTab().equals(settingsTab)) {
				UI.getCurrent().navigate(SettingsView.class);
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
		LOGGER.error(e.getMessage(), e);
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

	protected void showInfoNotification(String message) {
		Notification notification = Notification.show(message, 5000, Notification.Position.BOTTOM_CENTER);
		notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
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
}