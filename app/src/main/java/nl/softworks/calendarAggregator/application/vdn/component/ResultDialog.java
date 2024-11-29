package nl.softworks.calendarAggregator.application.vdn.component;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.textfield.TextArea;

public class ResultDialog extends CancelDialog {

    public ResultDialog(String contents) {
        super("Result");
        setSizeFull();

        TextArea textArea = new TextArea("", contents, "");
        textArea.setWidthFull();
        textArea.setHeight(95, Unit.PERCENTAGE);
        add(textArea);
        textArea.scrollToEnd(); // does not work
    }
}
