package nl.softworks.calendarAggregator.application.vdn.component;

import com.vaadin.flow.component.html.Anchor;
import org.tbee.webstack.vdn.component.fontawesome.FasAnchorIcon;

public class AnchorIcon extends FasAnchorIcon {
    public AnchorIcon(String href, String... classNames) {
        super(href, classNames);
    }

    // =================================
    // Predefined icons

    static public Anchor jumpOut(String href) {
        return href == null ? null : new FasAnchorIcon(href, "fas", "fa-arrow-up-right-from-square");
    }

    static public Anchor mapPin(String href) {
        return href == null ? null : new FasAnchorIcon(href, "fas", "fa-map-pin");
    }
}
