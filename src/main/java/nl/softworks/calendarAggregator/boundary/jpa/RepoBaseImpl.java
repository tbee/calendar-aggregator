package nl.softworks.calendarAggregator.boundary.jpa;

import jakarta.persistence.EntityManager;
import nl.softworks.calendarAggregator.domain.boundary.RepoBase;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;

public class RepoBaseImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID>
        implements RepoBase<T, ID> {

    private final EntityManager entityManager;

    public RepoBaseImpl(JpaEntityInformation entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void refresh(T t) {
        entityManager.refresh(t);
    }

    @Override
    public void clear() {
        entityManager.clear();
    }
}