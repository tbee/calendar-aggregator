package nl.softworks.calendarAggregator.application.vdn.component;

import com.google.common.collect.Table;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Harmonica is an Accordion where each tab can be expanded or collapsed.
 */
public class Harmonica extends VerticalLayout {

    record Entry(Accordion accordion, String summary) {}
    private final List<Entry> entries = new ArrayList<>();

    public void add(String summary, Component content) {
        Accordion accordion = new Accordion();
        accordion.add(summary, content);
        accordion.setWidthFull();
        add(accordion);
        entries.add(new Entry(accordion, summary));
    }

    public void open(String summary) {
        entries.stream()
                .filter(e -> e.summary.equals(summary))
                .forEach(e -> e.accordion.open(0));
    }

    public void close(String summary) {
        entries.stream()
                .filter(e -> e.summary.equals(summary))
                .forEach(e -> e.accordion.close());
    }
}
