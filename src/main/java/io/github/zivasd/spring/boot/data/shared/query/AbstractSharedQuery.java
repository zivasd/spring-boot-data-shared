package io.github.zivasd.spring.boot.data.shared.query;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.TupleElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.beans.TypeConverter;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.ResultProcessor;
import org.springframework.data.repository.query.ReturnedType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public abstract class AbstractSharedQuery implements RepositoryQuery {
	private final SharedQueryMethod queryMethod;
	private final EntityManager entityManager;

	protected AbstractSharedQuery(SharedQueryMethod queryMethod, EntityManager entityManager) {
		Assert.notNull(queryMethod, "Query method must not be null!");
		Assert.notNull(entityManager, "EntityManager must not be null!");
		this.queryMethod = queryMethod;
		this.entityManager = entityManager;
	}

	@Override
	@Nullable
	public Object execute(@NonNull Object[] parameters) {
		SharedParametersParameterAccessor accessor = new SharedParametersParameterAccessor(queryMethod.getParameters(),
				parameters);
		Object result = doExceute(accessor);
		final ReturnedType returnType = queryMethod.getResultProcessor().getReturnedType();
		if (returnType.getReturnedType().isInterface()) {
			ResultProcessor withDynamicProjection = queryMethod.getResultProcessor().withDynamicProjection(accessor);
			return withDynamicProjection.processResult(result,
					new TupleConverter(withDynamicProjection.getReturnedType()));
		} else {
			DataClassTupleConverter dataClassTupleConverter = DataClassTupleConverter
					.newInstance(returnType.getReturnedType());
			return queryMethod.getResultProcessor().processResult(result, dataClassTupleConverter);
		}
	}

	@Override
	@NonNull
	public SharedQueryMethod getQueryMethod() {
		return queryMethod;
	}

	protected EntityManager getEntityManager() {
		return entityManager;
	}

	protected abstract Object doExceute(SharedParametersParameterAccessor accessor);

	static class BeanPropertyTupleConverter implements Converter<Object, Object> {
		protected final Log logger = LogFactory.getLog(getClass());

		@Nullable
		private Class<?> mappedClass;

		@Nullable
		private ConversionService conversionService = DefaultConversionService.getSharedInstance();

		@Nullable
		private Map<String, PropertyDescriptor> mappedProperties;

		@Nullable
		private Set<String> mappedPropertyNames;

		public BeanPropertyTupleConverter() {
		}

		public BeanPropertyTupleConverter(Class<?> mappedClass) {
			initialize(mappedClass);
		}

		public void setMappedClass(Class<?> mappedClass) {
			if (this.mappedClass == null) {
				initialize(mappedClass);
			} else {
				if (this.mappedClass != mappedClass) {
					throw new InvalidDataAccessApiUsageException("The mapped class can not be reassigned to map to " +
							mappedClass + " since it is already providing mapping for " + this.mappedClass);
				}
			}
		}

		@Nullable
		public final Class<?> getMappedClass() {
			return this.mappedClass;
		}

		public void setConversionService(@Nullable ConversionService conversionService) {
			this.conversionService = conversionService;
		}

		@Nullable
		public ConversionService getConversionService() {
			return this.conversionService;
		}

		protected void initialize(Class<?> mappedClass) {
			this.mappedClass = mappedClass;
			this.mappedProperties = new HashMap<>();
			this.mappedPropertyNames = new HashSet<>();

			for (PropertyDescriptor pd : BeanUtils.getPropertyDescriptors(mappedClass)) {
				if (pd.getWriteMethod() != null) {
					this.mappedProperties.put(pd.getName(), pd);
					this.mappedPropertyNames.add(pd.getName());
				}
			}
		}

		@Override
		public Object convert(@NonNull Object source) {
			if (!(source instanceof Tuple)) {
				return source;
			}
			Tuple tuple = (Tuple) source;

			BeanWrapperImpl bw = new BeanWrapperImpl();
			initBeanWrapper(bw);

			Object mappedObject = constructMappedInstance(tuple, bw);
			bw.setBeanInstance(mappedObject);

			List<TupleElement<?>> tpels = tuple.getElements();
			for (int index = 0; index < tpels.size(); index++) {
				String property = tpels.get(index).getAlias();
				PropertyDescriptor pd = (this.mappedProperties != null ? this.mappedProperties.get(property) : null);
				if (pd != null) {
					try {
						Object value = tuple.get(index);
						bw.setPropertyValue(pd.getName(), value);
					} catch (NotWritablePropertyException ex) {
						throw new DataRetrievalFailureException(
								"Unable to map column '" + property + "' to property '" + pd.getName() + "'", ex);
					}
				}
			}
			return mappedObject;
		}

		protected Object constructMappedInstance(Tuple tuple, TypeConverter tc) {
			Assert.notNull(tuple, "Tuple must not null");
			Assert.notNull(tc, "TypeConverter must not null");
			Assert.state(this.mappedClass != null, "Mapped class was not specified");
			return BeanUtils.instantiateClass(this.mappedClass);
		}

		protected void initBeanWrapper(BeanWrapper bw) {
			ConversionService cs = getConversionService();
			if (cs != null) {
				bw.setConversionService(cs);
			}
		}

		public static BeanPropertyTupleConverter newInstance(Class<?> mappedClass) {
			return new BeanPropertyTupleConverter(mappedClass);
		}

		public static BeanPropertyTupleConverter newInstance(
				Class<?> mappedClass, @Nullable ConversionService conversionService) {
			BeanPropertyTupleConverter tupleMapper = newInstance(mappedClass);
			tupleMapper.setConversionService(conversionService);
			return tupleMapper;
		}
	}

	static class DataClassTupleConverter extends BeanPropertyTupleConverter {
		@Nullable
		private Constructor<?> mappedConstructor;

		@Nullable
		private String[] constructorParameterNames;

		@Nullable
		private TypeDescriptor[] constructorParameterTypes;

		public DataClassTupleConverter() {
		}

		public DataClassTupleConverter(Class<?> mappedClass) {
			super(mappedClass);
		}

		@Override
		protected void initialize(Class<?> mappedClass) {
			super.initialize(mappedClass);

			this.mappedConstructor = BeanUtils.getResolvableConstructor(mappedClass);
			int paramCount = this.mappedConstructor.getParameterCount();
			if (paramCount > 0) {
				this.constructorParameterNames = BeanUtils.getParameterNames(this.mappedConstructor);
				this.constructorParameterTypes = new TypeDescriptor[paramCount];
				for (int i = 0; i < paramCount; i++) {
					this.constructorParameterTypes[i] = new TypeDescriptor(
							new MethodParameter(this.mappedConstructor, i));
				}
			}
		}

		@Override
		protected Object constructMappedInstance(Tuple tuple, TypeConverter tc) {
			Assert.state(this.mappedConstructor != null, "Mapped constructor was not initialized");
			Object[] args;
			if (this.constructorParameterNames != null && this.constructorParameterTypes != null) {
				args = new Object[this.constructorParameterNames.length];
				for (int i = 0; i < args.length; i++) {
					String name = this.constructorParameterNames[i];
					Object value = tuple.get(name);
					TypeDescriptor td = this.constructorParameterTypes[i];
					args[i] = tc.convertIfNecessary(value, td.getType(), td);
				}
			} else {
				args = new Object[0];
			}
			return BeanUtils.instantiateClass(this.mappedConstructor, args);
		}

		public static DataClassTupleConverter newInstance(Class<?> mappedClass) {
			return new DataClassTupleConverter(mappedClass);
		}

		public static DataClassTupleConverter newInstance(
				Class<?> mappedClass, @Nullable ConversionService conversionService) {
			DataClassTupleConverter rowMapper = newInstance(mappedClass);
			rowMapper.setConversionService(conversionService);
			return rowMapper;
		}
	}

	static class TupleConverter implements Converter<Object, Object> {
		private final ReturnedType type;

		public TupleConverter(ReturnedType type) {
			Assert.notNull(type, "Returned type must not be null!");
			this.type = type;
		}

		@Override
		public Object convert(Object source) {
			if (!(source instanceof Tuple)) {
				return source;
			}
			Tuple tuple = (Tuple) source;
			List<TupleElement<?>> elements = tuple.getElements();
			if (elements.size() == 1) {
				Object value = tuple.get(elements.get(0));
				if (type.isInstance(value) || value == null) {
					return value;
				}
			}
			return new TupleBackedMap(tuple);
		}

		private static class TupleBackedMap implements Map<String, Object> {
			private static final String UNMODIFIABLE_MESSAGE = "A TupleBackedMap cannot be modified.";
			private final Tuple tuple;

			TupleBackedMap(Tuple tuple) {
				this.tuple = tuple;
			}

			@Override
			public int size() {
				return tuple.getElements().size();
			}

			@Override
			public boolean isEmpty() {
				return tuple.getElements().isEmpty();
			}

			@Override
			public boolean containsKey(Object key) {
				try {
					tuple.get((String) key);
					return true;
				} catch (IllegalArgumentException e) {
					return false;
				}
			}

			@Override
			public boolean containsValue(Object value) {
				return Arrays.asList(tuple.toArray()).contains(value);
			}

			@Override
			@Nullable
			public Object get(Object key) {
				if (!(key instanceof String)) {
					return null;
				}
				try {
					return tuple.get((String) key);
				} catch (IllegalArgumentException e) {
					return null;
				}
			}

			@Override
			public Object put(String key, Object value) {
				throw new UnsupportedOperationException(UNMODIFIABLE_MESSAGE);
			}

			@Override
			public Object remove(Object key) {
				throw new UnsupportedOperationException(UNMODIFIABLE_MESSAGE);
			}

			@Override
			public void putAll(Map<? extends String, ?> m) {
				throw new UnsupportedOperationException(UNMODIFIABLE_MESSAGE);
			}

			@Override
			public void clear() {
				throw new UnsupportedOperationException(UNMODIFIABLE_MESSAGE);
			}

			@Override
			public Set<String> keySet() {
				return tuple.getElements().stream() //
						.map(TupleElement::getAlias) //
						.collect(Collectors.toSet());
			}

			@Override
			public Collection<Object> values() {
				return Arrays.asList(tuple.toArray());
			}

			@Override
			public Set<Entry<String, Object>> entrySet() {
				return tuple.getElements().stream() //
						.map(e -> new HashMap.SimpleEntry<String, Object>(e.getAlias(), tuple.get(e))) //
						.collect(Collectors.toSet());
			}
		}
	}
}