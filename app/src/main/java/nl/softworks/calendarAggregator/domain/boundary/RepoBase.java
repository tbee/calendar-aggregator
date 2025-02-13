package nl.softworks.calendarAggregator.domain.boundary;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
public interface RepoBase<T, ID extends Serializable> extends JpaRepository<T, ID> {

    void clear();

    void refresh(T t);
}