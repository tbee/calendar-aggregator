package nl.softworks.calendarAggregator.domain.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import org.hibernate.Hibernate;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@MappedSuperclass
abstract public class EntityBase<T> implements Comparable<EntityBase<T>>  {
	transient
	protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	protected long id;
	static public final String ID = "id";
	public long id() {
		return id;
	}

	@Version
	protected long lazylock = 0;

	@Override
	public String toString() {
		return "id=" + id;
	}

	@Override
	public int hashCode() {
		long id = id();
		if (id == 0) {
			return System.identityHashCode(this);
		}
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (Hibernate.getClass(this) != Hibernate.getClass(obj)) return false;
		EntityBase<T> other = (EntityBase<T>) obj;
		long thisId = id();
		long otherId = other.id();
		if (thisId == 0 || otherId == 0) {
			return false;
		}
		return thisId == otherId;
	}

	@Override
	public int compareTo(@NotNull EntityBase<T> o) {
		return Long.compare(id, o.id);
	}
}
