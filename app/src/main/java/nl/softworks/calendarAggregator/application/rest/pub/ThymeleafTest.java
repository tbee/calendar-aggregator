package nl.softworks.calendarAggregator.application.rest.pub;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ThymeleafTest {
    @RequestMapping(value = "/index")
    public String index() {
        return "index";
    }
}
