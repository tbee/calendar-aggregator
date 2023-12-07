package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import nl.softworks.calendarAggregator.domain.boundary.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.jakarta.validator.UrlValidator;

@Entity
public class Settings extends EntityBase<Settings> {
	private static final Logger LOG = LoggerFactory.getLogger(Settings.class);

	static public Settings get() {
		Settings settings = R.settings().findById(1l).orElse(null);
		if (settings == null) {
			settings = new Settings();
		}
		return settings;
	}

	@Column(unique=true) // prevent the same name to occur
	@NotNull
	@UrlValidator
	protected String websiteBaseurl;
	static public final String WEBSITEBASEURL_PROPERTYID = "websiteBaseurl";
	public String websiteBaseurl() {
		return websiteBaseurl;
	}
	public Settings websiteBaseurl(String v) {
		this.websiteBaseurl = v;
		return this;
	}

	@NotNull
	protected String title;
	static public final String TITLE_PROPERTYID = "title";
	public String title() {
		return title;
	}
	public Settings title(String v) {
		this.title = v;
		return this;
	}

	@NotNull
	protected String subtitle;
	static public final String SUBTITLE_PROPERTYID = "subtitle";
	public String subtitle() {
		return subtitle;
	}
	public Settings subtitle(String v) {
		this.subtitle = v;
		return this;
	}

	@NotNull
	protected String disclaimer = "";
	static public final String DISCLAIMER_PROPERTYID = "disclaimer";
	public String disclaimer() {
		return disclaimer;
	}
	public Settings disclaimer(String v) {
		this.disclaimer = v;
		return this;
	}

	public String toString() {
		return super.toString() //
		     + ",websiteBaseurl=" + websiteBaseurl
		     ;
	}
}
