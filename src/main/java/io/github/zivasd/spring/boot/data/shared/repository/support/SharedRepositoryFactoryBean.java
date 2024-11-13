package io.github.zivasd.spring.boot.data.shared.repository.support;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.core.support.TransactionalRepositoryFactoryBeanSupport;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import io.github.zivasd.spring.boot.data.shared.repository.SharedRepository;

public class SharedRepositoryFactoryBean<T extends SharedRepository>
		extends TransactionalRepositoryFactoryBeanSupport<T, Void, Void> {

	private @Nullable EntityManager entityManager;
	private EscapeCharacter escapeCharacter = EscapeCharacter.DEFAULT;

	protected SharedRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
		super(repositoryInterface);
	}

	@Override
	protected RepositoryFactorySupport doCreateRepositoryFactory() {
		Assert.state(entityManager != null, "EntityManager must not be null!");
		SharedRepositoryFactory sharedRepositoryFactory = new SharedRepositoryFactory(entityManager);
		sharedRepositoryFactory.setEscapeCharacter(this.escapeCharacter);
		return sharedRepositoryFactory;
	}

	@PersistenceContext
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public void setMappingContext(MappingContext<?, ?> mappingContext) {
		super.setMappingContext(mappingContext);
	}

	@Override
	public void afterPropertiesSet() {
		Assert.state(entityManager != null, "EntityManager must not be null!");
		super.afterPropertiesSet();
	}

	public void setEscapeCharacter(char escapeCharacter) {
		this.escapeCharacter = EscapeCharacter.of(escapeCharacter);
	}
}
