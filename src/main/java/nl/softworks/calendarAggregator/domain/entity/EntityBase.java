package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@MappedSuperclass
abstract public class EntityBase<T> {
	transient
	protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	protected long id;
	static public final String ID_PROPERTYID = "id";
	public long id() {
		return id;
	}

	@Version
	protected long lazylock = 0;

	public String toString() {
		return super.toString() //
		     + ",id=" + id
			 + ",lazylock=" + lazylock
		     ;
	}
	@Override
	public int hashCode() {
		if (id == 0) {
			return super.hashCode();
		}
		return Objects.hash(id, lazylock);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		EntityBase other = (EntityBase) obj;
		if (id == 0) {
			return super.equals(obj);
		}
		return id == other.id //
//				&& lazylock == other.lazylock
				;
	}
}
