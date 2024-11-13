package io.github.zivasd.spring.boot.data.shared.repository.config;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import org.springframework.data.jpa.repository.config.JpaRepositoryConfigExtension;

import io.github.zivasd.spring.boot.data.shared.repository.SharedRepository;
import io.github.zivasd.spring.boot.data.shared.repository.support.SharedRepositoryFactoryBean;

public class SharedRepositoryConfigurationExtension extends JpaRepositoryConfigExtension {

	@Override
	public String getModuleName() {
		return "SHARED";
	}

	@Override
	public String getRepositoryFactoryBeanClassName() {
		return SharedRepositoryFactoryBean.class.getName();
	}

	@Override
	protected Collection<Class<? extends Annotation>> getIdentifyingAnnotations() {
		return Collections.emptySet();
	}

	@Override
	protected Collection<Class<?>> getIdentifyingTypes() {
		return Collections.<Class<?>>singleton(SharedRepository.class);
	}
}
