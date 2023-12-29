package nl.softworks.calendarAggregator.domain;

import java.util.Collections;
import java.util.List;

public class ValidationException extends IllegalArgumentException {

    private final List<String> messages;

    public ValidationException(String message) {
        super(message);
        this.messages = List.of(message);
    }

    public ValidationException(List<String> messages) {
        super(messages.get(0));
        this.messages = messages;
    }

    public List<String> getMessages() {
        return Collections.unmodifiableList(messages);
    }
}
