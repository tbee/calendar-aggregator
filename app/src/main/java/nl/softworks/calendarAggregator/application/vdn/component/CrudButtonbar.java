package nl.softworks.calendarAggregator.application.vdn.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.theme.lumo.LumoIcon;

public class CrudButtonbar extends HorizontalLayout {

    private Runnable onReload = null;
    private Runnable onInsert = null;
    private Runnable onEdit = null;
    private Runnable onDelete = null;
    private final Button reloadButton = new Button(LumoIcon.RELOAD.create(), e -> onReload.run());
    private final Button insertButton = new Button(LumoIcon.PLUS.create(), e -> onInsert.run());
    private final Button editButton = new Button(LumoIcon.EDIT.create(), e -> onEdit.run());
    private final Button deleteButton = new Button(LumoIcon.MINUS.create(), e -> onDelete.run());

    public CrudButtonbar() {
        this(false);
    }
    public CrudButtonbar(boolean vertical) {
        add(reloadButton, editButton, deleteButton, insertButton);
        setState();
    }

    private void setState() {
        reloadButton.setVisible(onReload != null);
        insertButton.setVisible(onInsert != null);
        editButton.setVisible(onEdit != null);
        deleteButton.setVisible(onDelete != null);
    }

    public CrudButtonbar onReload(Runnable v) {
        this.onReload = v;
        setState();
        return this;
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
