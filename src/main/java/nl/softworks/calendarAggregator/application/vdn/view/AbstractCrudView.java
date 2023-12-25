package nl.softworks.calendarAggregator.application.vdn.view;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import nl.softworks.calendarAggregator.application.vdn.CalendarAggregatorAppLayout;
import nl.softworks.calendarAggregator.application.vdn.component.CrudButtonbar;
import nl.softworks.calendarAggregator.application.vdn.component.OkCancelDialog;
import nl.softworks.calendarAggregator.application.vdn.form.AbstractCrudForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

abstract public class AbstractCrudView<E> extends CalendarAggregatorAppLayout
implements AfterNavigationObserver {
	private static final Logger LOG = LoggerFactory.getLogger(AbstractCrudView.class);

	private final Grid<E> treeGrid = new Grid<>();
	private final Supplier<AbstractCrudForm<E>> formSupplier;
	private final Consumer<E> saver;
	private final Consumer<E> deleter;
	private final Supplier<List<E>> finder;

	public AbstractCrudView(String title, Consumer<E> saver, Consumer<E> deleter, Supplier<List<E>> finder, Supplier<AbstractCrudForm<E>> formSupplier, Consumer<Grid<E>> setupTreeGrid) {
		super(title);
		this.formSupplier = formSupplier;
		this.saver = saver;
		this.deleter = deleter;
		this.finder = finder;

		// treeGrid
		setupTreeGrid.accept(treeGrid);

		// crudButtonbar
		CrudButtonbar crudButtonbar = new CrudButtonbar()
				.onReload(this::reloadGrid)
				.onInsert(() -> formSupplier.get().showInsertDialog(this::reloadGrid))
				.onEdit(this::edit)
				.onDelete(this::delete);

		// content
		setContent(new VerticalLayout(crudButtonbar, treeGrid));
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		reloadGrid();
	}

	private void edit() {
		E item = getSelectedItem();
		if (item == null) {
			return;
		}

		// Dialog
		AbstractCrudForm<E> form = formSupplier.get().populateWith(item);
		new OkCancelDialog(title, form)
				.okLabel("Save")
				.onOk(() -> {
					try {
						form.writeTo(item);
						saver.accept(item);
						reloadGrid();
					} catch (ValidationException e) {
						throw new RuntimeException(e);
					}
				})
				.open();
	}

	private void delete() {
		E item = getSelectedItem();
		if (item == null) {
			return;
		}

		new OkCancelDialog("Remove", new NativeLabel("Are you sure?"))
				.okLabel("Yes")
				.onOk(() -> {
					deleter.accept(item);
					reloadGrid();
				})
				.open();
	}

	private void reloadGrid() {
		// Remember selection
		E selectedItem = getSelectedItem();

		// Reload persons
		List<E> items = finder.get();
		treeGrid.setItems(items);

		// Reselect
		// TODO: make sure the select node is one from the treeNodes collection, not the old node, otherwise lazy lock goes wrong
//		if (selectedPerson != null) {
//			timezoneTreeGrid.select(selectedPerson);
//		}
	}

	private E getSelectedItem() {
		Set<E> selectedItems = treeGrid.getSelectedItems();
		if (selectedItems.isEmpty() || selectedItems.size() > 1) {
			return null;
		}
		E item = selectedItems.iterator().next();
		return item;
	}
}