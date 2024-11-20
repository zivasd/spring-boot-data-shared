package io.github.zivasd.spring.boot.data.shared.query;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.util.QueryExecutionConverters;
import org.springframework.data.util.Lazy;
import org.springframework.data.util.TypeInformation;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import io.github.zivasd.spring.boot.data.shared.repository.SharedQuery;
import io.github.zivasd.spring.boot.data.shared.repository.TableNameDecider;

public class SharedQueryMethod extends QueryMethod {
	private final Method method;
	private final Class<?> returnType;

	private final Lazy<SharedQuery> sharedQuery;
	private final Lazy<Boolean> isNativeQuery;
	private final Lazy<Class<? extends TableNameDecider>> tableNameDecider;

	protected SharedQueryMethod(Method method, RepositoryMetadata metadata, ProjectionFactory factory) {
		super(method, metadata, factory);

		Assert.notNull(method, "Method must not be null!");

		this.method = method;
		this.returnType = potentiallyUnwrapReturnTypeFor(metadata, method);

		this.sharedQuery = Lazy.of(() -> AnnotatedElementUtils.findMergedAnnotation(method, SharedQuery.class));
		this.isNativeQuery = Lazy.of(() -> getAnnotationValue("nativeQuery", Boolean.class));
		this.tableNameDecider = Lazy.of(() -> sharedQuery.get().tableNameDecider());

		assertParameterNamesInAnnotatedQuery();
	}

	private void assertParameterNamesInAnnotatedQuery() {
		String annotatedQuery = getAnnotatedQuery();
		for (Parameter parameter : getParameters()) {
			if (!parameter.isBindable()) {
				continue;
			}
			parameter.getName().ifPresent(name -> {
				if (!StringUtils.hasText(annotatedQuery)
						|| !annotatedQuery.contains(String.format(":%s", name))
								&& !annotatedQuery.contains(String.format("#%s", name))) {
					throw new IllegalStateException(
							String.format(
									"Using named parameters for method %s but parameter '%s' not found in annotated query '%s'!",
									method, parameter.getName(), annotatedQuery));
				}
			});

		}
	}

	@Override
	protected SharedParameters createParameters(Method method) {
		return new SharedParameters(method);
	}

	@Override
	public SharedParameters getParameters() {
		return (SharedParameters) super.getParameters();
	}

	SharedQuery getSharedQuery() {
		return this.sharedQuery.getNullable();
	}

	Class<?> getReturnType() {
		return returnType;
	}

	boolean isNativeQuery() {
		return this.isNativeQuery.get();
	}

	Class<? extends TableNameDecider> getTableNameDecider() {
		return this.tableNameDecider.get();
	}

	@Nullable
	String getAnnotatedQuery() {
		String query = getAnnotationValue("value", String.class);
		return StringUtils.hasText(query) ? query : null;
	}

	@Nullable
	String getAnnotatedCountQuery() {
		String countQuery = getAnnotationValue("countQuery", String.class);
		return StringUtils.hasText(countQuery) ? countQuery : null;
	}

	private <T> T getAnnotationValue(String attribute, Class<T> type) {
		return getMergedOrDefaultAnnotationValue(attribute, SharedQuery.class, type);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <T> T getMergedOrDefaultAnnotationValue(String attribute, Class annotationType, Class<T> targetType) {
		Annotation annotation = AnnotatedElementUtils.findMergedAnnotation(method, annotationType);
		if (annotation == null) {
			return targetType.cast(AnnotationUtils.getDefaultValue(annotationType, attribute));
		}
		return targetType.cast(AnnotationUtils.getValue(annotation, attribute));
	}

	private static Class<?> potentiallyUnwrapReturnTypeFor(RepositoryMetadata metadata, Method method) {
		TypeInformation<?> returnType = metadata.getReturnType(method);
		while (QueryExecutionConverters.supports(returnType.getType())
				|| QueryExecutionConverters.supportsUnwrapping(returnType.getType())) {
			returnType = returnType.getRequiredComponentType();
		}
		return returnType.getType();
	}
}
