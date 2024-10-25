package io.github.zivasd.spring.boot.data.shared.repository.support;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class SimpleSharedRepository implements SharedRepositoryImplementation {
	private final EntityManager entityManager;
	
	public SimpleSharedRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	
	protected EntityManager getEntityManager() {
		return entityManager;
	}
}
