package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import nl.softworks.calendarAggregator.domain.entity.validator.PersonPasswordValidator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Entity
@PersonPasswordValidator(message="Password cannot be empty")
public class Person extends EntityBase<Person> {

	@NotNull
	private String username;
	static public final String USERNAME_PROPERTYID = "username";
	public String getUsername() {
		return username;
	}
	public void setUsername(String code) {
		this.username = code;
	}

	@NotNull
	private String password;
	public String getPassword() {
		return this.password;
	}
	public void setPassword(String password) {
		if (password == null || password.isBlank()) {
			// silently ignore, apparently it need not be changed
			return;
		}
		this.password = new BCryptPasswordEncoder().encode(password);
	}
	public boolean matchesPassword(String password) {
		return new BCryptPasswordEncoder().matches(password, this.password);
	}

	@NotNull
	private String email;
	static public final String EMAIL_PROPERTYID = "email";
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

	@NotNull
	private String role;
	static public final String ROLE_PROPERTYID = "role";
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}

	static public enum Role {ROLE_USER, ROLE_PLANNER}

	@NotNull
	private boolean enabled = true;
	static public final String ENABLED_PROPERTYID = "enabled";
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String toString() {
		return super.toString() //
		     + ",username=" + username
		     ;
	}
}
