package nl.softworks.calendarAggregator.application.vdn.component;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.theme.lumo.LumoIcon;

public class CrudIconButtonbar extends HorizontalLayout {

    private Runnable onReload = null;
    private Runnable onInsert = null;
    private Runnable onEdit = null;
    private Runnable onDelete = null;
    private final IconButton reloadButton = new IconButton(LumoIcon.RELOAD.create(), e -> onReload.run());
    private final IconButton insertButton = new IconButton(LumoIcon.PLUS.create(), e -> onInsert.run());
    private final IconButton editButton = new IconButton(LumoIcon.EDIT.create(), e -> onEdit.run());
    private final IconButton deleteButton = new IconButton(LumoIcon.MINUS.create(), e -> onDelete.run());

    public CrudIconButtonbar() {
        this(false);
    }
    public CrudIconButtonbar(boolean vertical) {
        add(reloadButton, editButton, deleteButton, insertButton);
        setState();
    }

    private void setState() {
        reloadButton.setVisible(onReload != null);
        insertButton.setVisible(onInsert != null);
        editButton.setVisible(onEdit != null);
        deleteButton.setVisible(onDelete != null);
    }

    public CrudIconButtonbar onReload(Runnable v) {
        this.onReload = v;
        setState();
        return this;
    }

    public CrudIconButtonbar onInsert(Runnable v) {
        this.onInsert = v;
        setState();
        return this;
    }

    public CrudIconButtonbar onEdit(Runnable v) {
        this.onEdit = v;
        setState();
        return this;
    }

    public CrudIconButtonbar onDelete(Runnable v) {
        this.onDelete = v;
        setState();
        return this;
    }
}
