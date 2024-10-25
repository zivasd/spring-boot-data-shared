package io.github.zivasd.spring.boot.data.shared.query;

import java.lang.reflect.Method;

import javax.persistence.EntityManager;

import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public final class SharedQueryLookupStrategy {
	private SharedQueryLookupStrategy() {}
	
	private abstract static class AbstractQueryLookupStrategy implements QueryLookupStrategy {
		private final EntityManager entityManager;

		public AbstractQueryLookupStrategy(EntityManager entityManager) {
			Assert.notNull(entityManager, "EntityManager must not be null!");
			this.entityManager = entityManager;
		}
		
		@Override
		public final RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory,
				NamedQueries namedQueries) {
			SharedQueryMethod queryMethod = new SharedQueryMethod(method, metadata, factory);
			return resolveQuery(queryMethod, entityManager, namedQueries);
		}
		protected abstract RepositoryQuery resolveQuery(SharedQueryMethod method, EntityManager entityManager, NamedQueries namedQueries);
	}
	
	private static class CreateQueryLookupStrategy extends AbstractQueryLookupStrategy {
		public CreateQueryLookupStrategy(EntityManager entityManager) {
			super(entityManager);
		}

		@Override
		protected RepositoryQuery resolveQuery(SharedQueryMethod method, EntityManager entityManager, NamedQueries namedQueries) {
			return new NativeSharedQuery(method, entityManager);
		}
	}
	
	public static QueryLookupStrategy create(EntityManager entityManager, @Nullable Key key, QueryMethodEvaluationContextProvider evaluationContextProvider) {
		Assert.notNull(entityManager, "EntityManager must not be null!");
		Assert.notNull(evaluationContextProvider, "EvaluationContextProvider must not be null!");

		switch (key != null ? key : Key.CREATE_IF_NOT_FOUND) {
		case CREATE:
			return new CreateQueryLookupStrategy(entityManager);
		case USE_DECLARED_QUERY:
			return null;
		case CREATE_IF_NOT_FOUND:
			return new CreateQueryLookupStrategy(entityManager);
		default:
			throw new IllegalArgumentException(String.format("Unsupported query lookup strategy %s!", key));
		}
	}
}
