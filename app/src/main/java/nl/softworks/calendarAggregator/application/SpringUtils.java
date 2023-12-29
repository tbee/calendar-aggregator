package nl.softworks.calendarAggregator.application;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SpringUtils {

    static public String getLoggedInUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = (principal instanceof UserDetails userDetails) ? userDetails.getUsername() : principal.toString();
        return username;
    }

}
