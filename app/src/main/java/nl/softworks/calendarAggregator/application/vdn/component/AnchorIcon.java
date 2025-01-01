package nl.softworks.calendarAggregator.application.vdn.component;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;

public class AnchorIcon extends Anchor {

    public AnchorIcon(String href, String... classNames) {
        Span spanIcon = new Span();
        for (String className : classNames) {
            spanIcon.addClassName(className);
        }

        Span spanWrapper = new Span(spanIcon);
        spanWrapper.addClassName("icon");

        add(spanWrapper);
        setHref(href);
        setTarget("_blank");
    }

    public AnchorIcon href(String v) {
        setHref(v);
        return this;
    }

    // =================================
    // Predefined icons

    static public Anchor jumpOut(String href) {
        if (href == null) {
            return null;
        }
        return new AnchorIcon(href, "fas", "fa-arrow-up-right-from-square");
    }

    static public Anchor mapPin(String href) {
        if (href == null) {
            return null;
        }
        return new AnchorIcon(href, "fas", "fa-map-pin");
    }
}
