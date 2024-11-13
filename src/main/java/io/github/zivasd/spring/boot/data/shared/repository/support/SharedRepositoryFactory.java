package io.github.zivasd.spring.boot.data.shared.repository.support;

import java.util.Optional;

import javax.persistence.EntityManager;

import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import io.github.zivasd.spring.boot.data.shared.query.SharedQueryLookupStrategy;

public class SharedRepositoryFactory extends RepositoryFactorySupport {
	private final EntityManager entityManager;
	private EscapeCharacter escapeCharacter = EscapeCharacter.DEFAULT;

	public SharedRepositoryFactory(EntityManager entityManager) {
		Assert.notNull(entityManager, "EntityManager must not be null!");
		this.entityManager = entityManager;
	}

	public void setEscapeCharacter(EscapeCharacter escapeCharacter) {
		this.escapeCharacter = escapeCharacter;
	}

	/**
	 * extends JpaMetamodelEntityInformation and JpaPersistableEntityInformation
	 * 
	 */
	@Override
	public <T, I> EntityInformation<T, I> getEntityInformation(Class<T> domainClass) {
		return null;
	}

	@Override
	protected SharedRepositoryImplementation getTargetRepository(RepositoryInformation metadata) {
		return getTargetRepositoryViaReflection(metadata, entityManager);
	}

	@Override
	protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
		return SimpleSharedRepository.class;
	}

	@Override
	@NonNull
	protected Optional<QueryLookupStrategy> getQueryLookupStrategy(@Nullable Key key,
			@NonNull QueryMethodEvaluationContextProvider evaluationContextProvider) {
		return Optional
				.of(SharedQueryLookupStrategy.create(entityManager, key, evaluationContextProvider, escapeCharacter));
	}
}
