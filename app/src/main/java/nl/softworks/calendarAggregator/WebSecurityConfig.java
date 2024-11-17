package nl.softworks.calendarAggregator;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// https://www.baeldung.com/spring-boot-security-autoconfiguration
// https://tutorialmeta.com/question/server-connection-lost-after-successful-login-with-spring-security

@Configuration
@EnableWebSecurity 
public class WebSecurityConfig extends VaadinWebSecurity {

    @Autowired
    private DataSource dataSource;

    @Autowired
    public void configAuthentication(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication().passwordEncoder(new BCryptPasswordEncoder()) //
            .dataSource(dataSource) //
            .usersByUsernameQuery("select username, password, enabled from person where person.username=?") //
            .authoritiesByUsernameQuery("select username, role from person where person.username=?") //
        ;
    }

    @Bean(name = "VaadinSecurityFilterChainBean")
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .authorizeHttpRequests(authorize -> authorize.requestMatchers("/app/**").authenticated())
                .authorizeHttpRequests(authorize -> authorize.requestMatchers("/**").permitAll())
                .headers(headers -> headers.frameOptions(config -> config.sameOrigin()))
                .formLogin(config -> config.defaultSuccessUrl("/app", true))
                .httpBasic(Customizer.withDefaults())
                .csrf(config -> config.disable())
                .logout(config -> config.invalidateHttpSession(true).deleteCookies("JSESSIONID"));
        super.configure(httpSecurity);
        return httpSecurity.build();
    }


    // ==================================================================
    // SESSIONS

    private final List<HttpSession> sessions = new ArrayList<>();

    public List<HttpSession> getActiveSessions() {
        return Collections.unmodifiableList(sessions);
    }

    @Bean
    public HttpSessionListener httpSessionListener() {
        // invalidating the session assigns it a new id, so we can't use that as the identified in a map setup
        return new HttpSessionListener() {
            @Override
            public void sessionCreated(HttpSessionEvent hse) {
                sessions.add(hse.getSession());
            }

            @Override
            public void sessionDestroyed(HttpSessionEvent hse) {
                sessions.remove(hse.getSession());
            }
        };
    }
}

