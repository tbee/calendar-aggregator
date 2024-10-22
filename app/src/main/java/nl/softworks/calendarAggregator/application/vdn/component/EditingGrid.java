package nl.softworks.calendarAggregator.application.vdn.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Setter;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.ValueProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class EditingGrid<BEAN> extends Grid<BEAN> {

    private final List<BEAN> items = new ArrayList<>();
    private final Editor<BEAN> editor;
    private final Binder<BEAN> binder;
    private Consumer<BEAN> onEdit = null;

    public EditingGrid(Class<BEAN> beanType, boolean autoCreateColumns) {
        super(beanType, autoCreateColumns);
        editor = getEditor();
        binder = new Binder<>(beanType);
        construct(beanType);
    }

    public EditingGrid(Class<BEAN> beanType) {
        super(beanType);
        editor = getEditor();
        binder = new Binder<>(beanType);
        construct(beanType);
    }

    public void construct(Class<BEAN> beanType) {
        // Also allow inline editing. See what is more pleasant (because it is a different UX).
        // See https://vaadin.com/forum/t/consume-key-event/166801/6
        editor.setBinder(binder);
        editor.setBuffered(true);

        addItemDoubleClickListener(e -> {
            if (!editor.isOpen()) {
                editor.editItem(e.getItem());
                Component editorComponent = e.getColumn().getEditorComponent();
                if (editorComponent instanceof Focusable) {
                    ((Focusable) editorComponent).focus();
                }
            }
        });
    }

    public void refresh() {
        setItems(new ListDataProvider<>(this.items));
    }

    public List<BEAN> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void setItems(List<BEAN> items) {
        this.items.clear();
        this.items.addAll(items);
        setItems(new ListDataProvider<>(this.items));
    }

    public void addItems(BEAN... items) {
        this.items.addAll(Arrays.asList(items));
        setItems(new ListDataProvider<>(this.items));
    }

    public void removeItems(BEAN... items) {
        this.items.removeAll(Arrays.asList(items));
        setItems(new ListDataProvider<>(this.items));
    }

    public Column<BEAN> addCrudIconButtonbarColumn() {
        return addComponentColumn((ValueProvider<BEAN, Component>) bean -> new CrudIconButtonbar()
                .onEdit(EditingGrid.this.onEdit == null ? null : () -> EditingGrid.this.onEdit.accept(bean))
                .onDelete(() -> {
                    new OkCancelDialog("Remove item", new NativeLabel("Are you sure?"))
                            .okLabel("Yes")
                            .onOk(() -> {
                                removeItems(bean);
                            })
                            .open();
                }));
    }

    public void onEdit(Consumer<BEAN> consumer) {
        this.onEdit = consumer;
    }

    public Column<BEAN> addStringColumn(ValueProvider<BEAN, String> getter, Setter<BEAN, String> setter) {
        Grid.Column<BEAN> column = addColumn(getter);

        TextField textField = new TextField();
        textField.setWidthFull();
        column.setEditorComponent(textField);
        binder.forField(textField).bind(getter, setter);

        textField.getElement().addEventListener("keydown", e -> {
            editor.cancel();
        }).setFilter("event.code === 'Escape'").addEventData("event.stopPropagation()");

//        textField.addBlurListener(e -> {
//            if (editor.isOpen()) {
//                editor.save();
//            }
//        });

        textField.getElement().addEventListener("keydown", e -> {
            if (editor.isOpen()) {
                editor.save();
            }
        }).setFilter("event.code === 'Enter'").addEventData("event.stopPropagation()");

        return column;
    }
}
