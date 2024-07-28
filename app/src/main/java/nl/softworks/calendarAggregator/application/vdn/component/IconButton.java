package nl.softworks.calendarAggregator.application.vdn.component;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class IconButton extends HorizontalLayout {

    public IconButton(Icon icon, ComponentEventListener<ClickEvent<Icon>> clickListener) {
        icon.addClickListener(clickListener);
        icon.getElement().setAttribute("style", "cursor: pointer;");
        add(icon);
    }
}
