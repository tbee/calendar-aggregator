package nl.softworks.calendarAggregator.application.vdn.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.theme.lumo.LumoIcon;

public class OkCancelDialog extends Dialog {

    Button okButton = new Button("Ok");
    private Runnable onOk = null;

    public OkCancelDialog(String title) {
        this(title, null);
    }

    public OkCancelDialog(String title, Component component) {
        setHeaderTitle(title);

        // Close
        Button closeButton = new Button(LumoIcon.CROSS.create(), e -> close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        getHeader().add(closeButton);

        // Ok
        okButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        okButton.addClickListener(e -> ok());
        getFooter().add(okButton);

        // Content
        if (component != null) {
            add(component);
        }
    }

    private void ok() {
        if (onOk != null) {
            onOk.run();
        }
        close();
    }

    public OkCancelDialog onOk(Runnable v) {
        this.onOk = v;
        return this;
    }

    public OkCancelDialog okLabel(String v) {
        this.okButton.setText(v);
        return this;
    }

    public OkCancelDialog width(float width, Unit unit) {
        this.setWidth(width, unit);
        return this;
    }
}
