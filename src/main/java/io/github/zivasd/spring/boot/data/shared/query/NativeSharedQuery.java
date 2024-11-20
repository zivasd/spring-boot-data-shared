package io.github.zivasd.spring.boot.data.shared.query;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.compile;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Tuple;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.util.Lazy;
import org.springframework.util.StringUtils;

import io.github.zivasd.spring.boot.data.shared.repository.TableNameDecider;
import io.github.zivasd.spring.boot.data.shared.repository.config.ModuleConfiguration;

/**
 * for isCollectionQuery isPageQuery
 * isStreamQuery isSliceQuery isModifyingQuery ... not support
 */
public class NativeSharedQuery extends AbstractSharedQuery {
	private static final Pattern COUNT_MATCH = compile("\\s*(select\\s+(((?s).+?)?)\\s+)(from\\s+)(.*)",
			CASE_INSENSITIVE | DOTALL);
	private static final String TABLE_NAME_PLACEHOLDER = "$TABLE$";
	private static final ConversionService CONVERSION_SERVICE;
	private final Lazy<TableNameDecider> tableNameDecider;

	static {
		ConfigurableConversionService conversionService = new DefaultConversionService();
		conversionService.removeConvertible(Collection.class, Object.class);
		conversionService.removeConvertible(Object.class, Optional.class);
		CONVERSION_SERVICE = conversionService;
	}

	public NativeSharedQuery(SharedQueryMethod method, EntityManager entityManager) {
		super(method, entityManager);
		this.tableNameDecider = Lazy.of(() -> createTableNameDecider(method.getTableNameDecider()));
	}

	private TableNameDecider createTableNameDecider(Class<? extends TableNameDecider> clazz) {
		if (clazz == TableNameDecider.NoOperator.class) {
			return TableNameDecider.NoOperator.INSTANCE;
		}
		return ModuleConfiguration.autowire(clazz);
	}

	private List<String> deciderTableNames(SharedParametersParameterAccessor accessor) {
		Map<String, Object> decideParameters = new HashMap<>();
		for (Parameter param : accessor.getParameters().getDeciderParamParameter()) {
			String name = param.getName().orElse(Integer.toString(param.getIndex()));
			decideParameters.put(name, accessor.getValue(param));
		}
		TableNameDecider paramDecider = accessor.getTableNameDecider();
		if (paramDecider instanceof TableNameDecider.NoOperator)
			return this.tableNameDecider.get().decideNames(decideParameters);
		else
			return paramDecider.decideNames(decideParameters);
	}

	@Override
	protected Object doExceute(SharedParametersParameterAccessor accessor) {
		final SharedQueryMethod queryMethod = getQueryMethod();
		List<String> tableNames = deciderTableNames(accessor);
		String queryString = deriveQuery(queryMethod, tableNames);
		if (tableNames.isEmpty() && queryString.contains(TABLE_NAME_PLACEHOLDER)) {
			return Collections.emptyList();
		}

		Map<String, Object> bindableParameters = new HashMap<>();
		for (Parameter param : accessor.getParameters().getBindableParameters()) {
			param.getName().ifPresent(name -> bindableParameters.put(name, accessor.getValue(param)));
		}
		final EntityManager em = getEntityManager();
		Query query = em.createNativeQuery(queryString + " " + deriveSort(accessor.getSort()), Tuple.class);
		bindableParameters.forEach(query::setParameter);

		if (queryMethod.isPageQuery()) {
			Pageable pageable = accessor.getPageable();
			if (!pageable.isUnpaged()) {
				query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
				query.setMaxResults(pageable.getPageSize());
			}
			Query countQuery = em.createNativeQuery(deriveCountQuery(queryMethod, tableNames));
			bindableParameters.forEach(countQuery::setParameter);
			Object countObject = countQuery.getSingleResult();
			Long count = CONVERSION_SERVICE.convert(countObject, Long.class);
			List<?> listResult = query.getResultList();
			return new PageImpl<>(listResult, accessor.getPageable(), count == null ? 0 : count);
		} else if (queryMethod.isCollectionQuery()) {
			return query.getResultList();
		} else {
			return query.getSingleResult();
		}
	}

	private String deriveSort(Sort sort) {
		if (sort.isUnsorted())
			return "";

		return sort.stream().map(
				order -> String.format("ORDER BY %s %s", order.getProperty(), getDirectionString(order.getDirection())))
				.reduce((result, element) -> result + " , " + element).orElse("");
	}

	private String getDirectionString(Direction direction) {
		return direction.equals(Direction.ASC) ? "ASC" : "DESC";
	}

	private String deriveCountQuery(SharedQueryMethod queryMethod, List<String> tableNames) {
		String singleQuery = deriveCountQuery(queryMethod);
		String query = tableNames.isEmpty() ? singleQuery
				: tableNames.stream().map(e -> singleQuery.replace(TABLE_NAME_PLACEHOLDER, e))
						.reduce((result, element) -> result + " UNION ALL " + element).orElse(null);
		return String.format("SELECT sum(scount) FROM (%s)", query);
	}

	private String deriveCountQuery(SharedQueryMethod queryMethod) {
		String queryString = queryMethod.getAnnotatedCountQuery();
		if (queryString != null) {
			return queryString;
		}
		queryString = queryMethod.getAnnotatedQuery();
		Matcher matcher = COUNT_MATCH.matcher(queryString);
		if (matcher.matches()) {
			queryString = String.format("select count(*) as scount from %s", matcher.group(5));
		}
		return queryString;
	}

	private String deriveQuery(SharedQueryMethod queryMethod, List<String> tableNames) {
		String queryString = queryMethod.getAnnotatedQuery();
		if (!StringUtils.hasText(queryString)) {
			throw new InvalidDataAccessApiUsageException("The annotation SharedQuery must set query value.");
		}
		return tableNames.isEmpty() ? queryString
				: tableNames.stream().map(e -> queryString.replace(TABLE_NAME_PLACEHOLDER, e))
						.reduce((result, element) -> result + " UNION ALL " + element).orElse(null);
	}
}
