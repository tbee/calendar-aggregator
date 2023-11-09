package nl.softworks.calendarAggregator.boundary.vdn;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class CrudButtonbar extends HorizontalLayout {

    Runnable onInsert = null;
    Runnable onEdit = null;
    Runnable onDelete = null;

    public CrudButtonbar() {
        Button insertButton = new Button(VaadinIcon.PLUS.create(), e -> onInsert.run());
        Button editButton = new Button(VaadinIcon.EDIT.create(), e -> onEdit.run());
        Button deleteButton = new Button(VaadinIcon.TRASH.create(), e -> onDelete.run());
        add(insertButton, editButton, deleteButton);
    }

    public CrudButtonbar onInsert(Runnable v) {
        this.onInsert = v;
        return this;
    }

    public CrudButtonbar onEdit(Runnable v) {
        this.onEdit = v;
        return this;
    }

    public CrudButtonbar onDelete(Runnable v) {
        this.onDelete = v;
        return this;
    }
}
