package nl.softworks.calendarAggregator.application.vdn.component;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;

public class AnchorIcon {

    static public Anchor jumpOut(String href) {
        if (href == null) {
            return null;
        }

        Span spanIcon = new Span();
        spanIcon.addClassName("fas");
        spanIcon.addClassName("fa-arrow-up-right-from-square");

        Span spanWrapper = new Span(spanIcon);
        spanWrapper.addClassName("icon");

        Anchor anchor = new Anchor(href, spanWrapper);
        anchor.setTarget("_blank");
        return anchor;
    }
}
