package nl.softworks.calendarAggregator;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

/**
 * https://www.baeldung.com/spring-boot-internationalization
 * By default, a Spring Boot application will look for message files containing internationalization keys and values in the src/main/resources folder.
 * The file for the default locale will have the name messages.properties, and files for each locale will be named messages_XX.properties, where XX is the locale code.
 * The keys for the values that will be localized have to be the same in every file, with values appropriate to the language they correspond to.
 */
public class WebMvcConfig implements WebMvcConfigurer {

    /*
     * Location and encoding of the messages
     */
    @Bean("messageSource")
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        // use default location '/': messageSource.setBasenames("lang/messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    /*
     * To be able to specify a locale for an HTTP Request. (lang=)
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("lang");
        registry.addInterceptor(localeChangeInterceptor);
    }
}
