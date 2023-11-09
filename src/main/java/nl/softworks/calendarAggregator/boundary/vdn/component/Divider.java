package nl.softworks.calendarAggregator.boundary.vdn.component;

import com.vaadin.flow.component.html.Span;

public class Divider extends Span {

    public Divider() {
        getStyle().set("background-color", "silver");
        getStyle().set("flex", "0 0 2px");
        getStyle().set("align-self", "stretch");
        getStyle().set("margin", "5px");
    }
}