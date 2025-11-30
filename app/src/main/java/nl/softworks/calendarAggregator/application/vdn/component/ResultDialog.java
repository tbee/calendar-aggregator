package nl.softworks.calendarAggregator.application.vdn.component;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.textfield.TextArea;
import org.tbee.webstack.vdn.component.ConfirmationDialog;

public class ResultDialog extends ConfirmationDialog {

    public ResultDialog(String contents) {
        super("Result");
        setSizeFull();
        confirmable();
        escapeIsCancel();

        TextArea textArea = new TextArea("", contents, "");
        textArea.setWidthFull();
        textArea.setHeight(95, Unit.PERCENTAGE);
        add(textArea);
        textArea.scrollToEnd(); // does not work
    }
}
