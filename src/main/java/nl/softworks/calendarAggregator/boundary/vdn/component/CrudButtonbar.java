package nl.softworks.calendarAggregator.boundary.vdn.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class CrudButtonbar extends HorizontalLayout {

    Runnable onInsert = null;
    Runnable onEdit = null;
    Runnable onDelete = null;
    Button insertButton = new Button(VaadinIcon.PLUS.create(), e -> onInsert.run());
    Button editButton = new Button(VaadinIcon.EDIT.create(), e -> onEdit.run());
    Button deleteButton = new Button(VaadinIcon.TRASH.create(), e -> onDelete.run());

    public CrudButtonbar() {
        add(insertButton, editButton, deleteButton);
        setState();
    }

    private void setState() {
        insertButton.setVisible(onInsert != null);
        editButton.setVisible(onEdit != null);
        deleteButton.setVisible(onDelete != null);
    }

    public CrudButtonbar onInsert(Runnable v) {
        this.onInsert = v;
        setState();
        return this;
    }

    public CrudButtonbar onEdit(Runnable v) {
        this.onEdit = v;
        setState();
        return this;
    }

    public CrudButtonbar onDelete(Runnable v) {
        this.onDelete = v;
        setState();
        return this;
    }
}
