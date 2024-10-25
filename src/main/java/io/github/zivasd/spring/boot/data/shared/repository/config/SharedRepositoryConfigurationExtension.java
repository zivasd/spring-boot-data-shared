package io.github.zivasd.spring.boot.data.shared.repository.config;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource;
import org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport;
import org.springframework.data.repository.config.RepositoryConfigurationSource;
import org.springframework.data.repository.config.XmlRepositoryConfigurationSource;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import io.github.zivasd.spring.boot.data.shared.repository.SharedRepository;
import io.github.zivasd.spring.boot.data.shared.repository.support.SharedRepositoryFactoryBean;

public class SharedRepositoryConfigurationExtension extends RepositoryConfigurationExtensionSupport {
	private static final String DEFAULT_TRANSACTION_MANAGER_BEAN_NAME = "transactionManager";
	private static final String TRANSACTION_MANAGER_ATTRIBUTE = "transactionManager";
	private static final String ENABLE_DEFAULT_TRANSACTIONS_ATTRIBUTE = "enableDefaultTransactions";
	
	@Override
	public String getModuleName() {
		return "SHARED";
	}
	
	@Override
	public String getRepositoryFactoryBeanClassName() {
		return SharedRepositoryFactoryBean.class.getName();
	}
	
	@Override
	protected String getModulePrefix() {
		return getModuleName().toLowerCase(Locale.US);
	}
	
	@Override
	protected Collection<Class<? extends Annotation>> getIdentifyingAnnotations() {
		return Collections.emptySet();
	}

	@Override
	protected Collection<Class<?>> getIdentifyingTypes() {
		return Collections.<Class<?>> singleton(SharedRepository.class);
	}

	@Override
	public void postProcess(BeanDefinitionBuilder builder, RepositoryConfigurationSource source) {
		Optional<String> transactionManagerRef = source.getAttribute("transactionManagerRef");
		builder.addPropertyValue(TRANSACTION_MANAGER_ATTRIBUTE, transactionManagerRef.orElse(DEFAULT_TRANSACTION_MANAGER_BEAN_NAME));
		builder.addPropertyValue("entityManager", getEntityManagerBeanDefinitionFor(source, source.getSource()));
	}
	
	@Override
	public void postProcess(BeanDefinitionBuilder builder, AnnotationRepositoryConfigurationSource config) {
		AnnotationAttributes attributes = config.getAttributes();
		builder.addPropertyValue(ENABLE_DEFAULT_TRANSACTIONS_ATTRIBUTE,
				attributes.getBoolean(ENABLE_DEFAULT_TRANSACTIONS_ATTRIBUTE));
	}

	@Override
	public void postProcess(BeanDefinitionBuilder builder, XmlRepositoryConfigurationSource config) {
		Optional<String> enableDefaultTransactions = config.getAttribute(ENABLE_DEFAULT_TRANSACTIONS_ATTRIBUTE);
		if (enableDefaultTransactions.isPresent() && StringUtils.hasText(enableDefaultTransactions.get())) {
			builder.addPropertyValue(ENABLE_DEFAULT_TRANSACTIONS_ATTRIBUTE, enableDefaultTransactions.get());
		}		
	}
	
	private static AbstractBeanDefinition getEntityManagerBeanDefinitionFor(RepositoryConfigurationSource config,
			@Nullable Object source) {

		BeanDefinitionBuilder builder = BeanDefinitionBuilder
				.rootBeanDefinition("org.springframework.orm.jpa.SharedEntityManagerCreator");
		builder.setFactoryMethod("createSharedEntityManager");
		builder.addConstructorArgReference(getEntityManagerBeanRef(config));

		AbstractBeanDefinition bean = builder.getRawBeanDefinition();
		bean.setSource(source);

		return bean;
	}
	
	private static String getEntityManagerBeanRef(RepositoryConfigurationSource config) {

		Optional<String> entityManagerFactoryRef = config.getAttribute("entityManagerFactoryRef");
		return entityManagerFactoryRef.orElse("entityManagerFactory");
	}
}
