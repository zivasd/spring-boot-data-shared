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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.repository.query.Parameter;

/**
 * for isCollectionQuery isPageQuery
 * isStreamQuery isSliceQuery isModifyingQuery ... not support
 */
public class NativeSharedQuery extends AbstractSharedQuery {
	private static final Logger logger = LoggerFactory.getLogger(NativeSharedQuery.class);
	private static final Pattern COUNT_MATCH = compile("\\s*(select\\s+(((?s).+?)?)\\s+)(from\\s+)(.*)", CASE_INSENSITIVE | DOTALL);
	private static final ConversionService CONVERSION_SERVICE;

	static {
		ConfigurableConversionService conversionService = new DefaultConversionService();
		conversionService.removeConvertible(Collection.class, Object.class);
		conversionService.removeConvertible(Object.class, Optional.class);
		CONVERSION_SERVICE = conversionService;
	}

	public NativeSharedQuery(SharedQueryMethod method, EntityManager entityManager) {
		super(method, entityManager);
	}

	@Override
	protected Object doExceute(SharedParametersParameterAccessor accessor) {
		Map<String, Object> bindableParameters = new HashMap<>();
		for(Parameter param : accessor.getParameters().getBindableParameters()) {
			param.getName().ifPresent(name->bindableParameters.put(name, accessor.getValue(param)));
		}
		
		final SharedQueryMethod queryMethod = getQueryMethod();
		List<String> tableNames = Collections.emptyList();
		try {
			tableNames = queryMethod.getTableNameDecider().newInstance().decideNames(bindableParameters);
		} catch (InstantiationException | IllegalAccessException e) {
			logger.error("Cant create instance of {}",queryMethod.getTableNameDecider().getName(), e);
			return null;
		}

		String queryString = deriveQuery(queryMethod, tableNames);
		final EntityManager em = getEntityManager();
		Query query = em.createNativeQuery(queryString+" "+deriveSort(accessor.getSort()), Tuple.class);
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
			return new PageImpl<>(listResult, accessor.getPageable(), count==null?0:count);
		} else if (queryMethod.isCollectionQuery()){
			return query.getResultList();
		} else {
			List<?> resultList = query.getResultList();
			return resultList.isEmpty()?null:resultList.get(0);
		}
	}
	
	private String deriveSort(Sort sort) {
		if(sort.isUnsorted())
			return "";
		
		return sort.stream().map(order->String.format("ORDER BY %s %s", order.getProperty(), getDirectionString(order.getDirection())))
			.reduce((result, element)-> result + " , " + element).orElse("");
	}
	
	private String getDirectionString(Direction direction) {
		return direction.equals(Direction.ASC)?"ASC":"DESC";
	}
	
	private String deriveCountQuery(SharedQueryMethod queryMethod, List<String> tableNames) {
		String singleQuery = deriveCountQuery(queryMethod);
		String query = tableNames.isEmpty() ? singleQuery : tableNames.stream().map(e -> singleQuery.replace("$TABLE$", e))
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
		if (queryString == null) {
			throw new InvalidDataAccessApiUsageException("The annotation SharedQuery must set query value.");
		}
		return tableNames.isEmpty() ? queryString : tableNames.stream().map(e -> queryString.replace("$TABLE$", e))
				.reduce((result, element) -> result + " UNION ALL " + element).orElse(null);
	}
}
