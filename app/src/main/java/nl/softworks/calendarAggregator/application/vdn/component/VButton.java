package nl.softworks.calendarAggregator.application.vdn.component;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;

public class VButton extends Button {
    public VButton() {
    }

    public VButton(String text) {
        super(text);
    }

    public VButton(Component icon) {
        super(icon);
    }

    public VButton(String text, Component icon) {
        super(text, icon);
    }

    public VButton(String text, ComponentEventListener<ClickEvent<Button>> clickListener) {
        super(text, clickListener);
    }

    public VButton(Component icon, ComponentEventListener<ClickEvent<Button>> clickListener) {
        super(icon, clickListener);
    }

    public VButton(String text, Component icon, ComponentEventListener<ClickEvent<Button>> clickListener) {
        super(text, icon, clickListener);
    }

    public VButton withIsPrimary(boolean isPrimary) {
        if (isPrimary) {
            addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        }
        return this;
    }
    public VButton withIsPrimary() {
        return withIsPrimary(true);
    }
}
