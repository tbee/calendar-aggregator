package nl.softworks.calendarAggregator.application.vdn.form;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.data.binder.ValidationException;

abstract public class AbstractCrudForm<E> extends FormLayout {

	abstract public AbstractCrudForm<E> populateWith(E item);
	abstract public AbstractCrudForm<E> writeTo(E item) throws ValidationException;
}