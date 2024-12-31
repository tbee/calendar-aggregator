package nl.softworks.calendarAggregator.application.vdn.component;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;

public class AnchorIcon {

    private final Anchor anchor;

    public AnchorIcon(String href, String... classNames) {
        Span spanIcon = new Span();
        for (String className : classNames) {
            spanIcon.addClassName(className);
        }

        Span spanWrapper = new Span(spanIcon);
        spanWrapper.addClassName("icon");

        anchor = new Anchor(href, spanWrapper);
        anchor.setTarget("_blank");
    }

    public AnchorIcon href(String v) {
        anchor.setHref(v);
        return this;
    }

    static public Anchor jumpOut(String href) {
        if (href == null) {
            return null;
        }
        return new AnchorIcon(href, "fas", "fa-arrow-up-right-from-square").anchor;
    }

    static public Anchor mapPin(String href) {
        if (href == null) {
            return null;
        }
        return new AnchorIcon(href, "fas", "fa-map-pin").anchor;
    }
}
