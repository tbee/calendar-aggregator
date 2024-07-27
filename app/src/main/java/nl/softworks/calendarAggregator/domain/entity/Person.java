package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import nl.softworks.calendarAggregator.application.jpa.PersonRoleConverter;
import nl.softworks.calendarAggregator.domain.entity.validator.PersonPasswordValidator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Entity
@PersonPasswordValidator(message="Password cannot be empty")
public class Person extends EntityBase<Person> {

	@NotNull
	@Column(unique=true)
	private String username;
	static public final String USERNAME = "username";
	public String username() {
		return username;
	}
	public Person username(String code) {
		this.username = code;
		return this;
	}

	@NotNull
	private String password;
	static public final String PASSWORD = "password";
	public String password() {
		return this.password;
	}
	public Person password(String password) {
		if (password == null || password.isBlank()) {
			// silently ignore, apparently it need not be changed
			return null;
		}
		this.password = new BCryptPasswordEncoder().encode(password);
		return this;
	}
	public boolean matchesPassword(String password) {
		return new BCryptPasswordEncoder().matches(password, this.password);
	}

	@NotNull
	private String email;
	static public final String EMAIL = "email";
	public String email() {
		return email;
	}
	public Person email(String email) {
		this.email = email;
		return this;
	}

	@NotNull
	@Convert(converter = PersonRoleConverter.class)
	private Role role = Role.ROLE_USER;
	static public final String ROLE = "role";
	public Role role() {
		return role;
	}
	public Person role(Role role) {
		this.role = role;
		return this;
	}

	public enum Role {ROLE_USER, ROLE_ADMIN}

	@NotNull
	private boolean enabled = true;
	static public final String ENABLED = "enabled";
	public boolean enabled() {
		return enabled;
	}
	public Person enabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	public String toString() {
		return super.toString() //
		     + ",username=" + username
		     ;
	}
}
