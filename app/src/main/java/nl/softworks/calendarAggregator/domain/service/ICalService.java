package nl.softworks.calendarAggregator.domain.service;

import java.util.Arrays;
import java.util.regex.Pattern;

public class ICalService {

    public String sanatize(String ical) {
        return sed(ical, "^DTSTAMP:([0-9]{8}T[0-9]{6}$)", "DTSTAMP:$1Z");
    }

    private String sed(String input, String searchPattern, String replacement) {
        // Make sure we only have newlines, so we can split on them. The linefeed is appended again after the regex.
        input = input
                .replaceAll("\r", "")
                .replaceAll("\n\n", "\n");
        Pattern pattern = Pattern.compile(searchPattern);
        return Arrays.stream(input.split("\n"))
                .map(line -> {
                    String replaced = pattern.matcher(line).replaceAll(replacement);
                    //if (!line.equals(replaced)) {
                    //    System.out.println("replaced " + line + " with " + replaced);
                    //}
                    return replaced + "\r\n";
                })
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }
}
