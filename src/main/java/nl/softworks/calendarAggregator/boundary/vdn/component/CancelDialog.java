package nl.softworks.calendarAggregator.boundary.vdn.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.theme.lumo.LumoIcon;

public class CancelDialog extends Dialog {

    public CancelDialog(String title) {
        this(title, null);
    }

    public CancelDialog(String title, Component component) {
        setHeaderTitle(title);

        // Close
        Button closeButton = new Button(LumoIcon.CROSS.create(), e -> close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        getHeader().add(closeButton);

        // Content
        if (component != null) {
            add(component);
        }
    }
}
