package io.github.zivasd.spring.boot.data.shared.repository.config;

import java.lang.annotation.Annotation;

import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

public class SharedRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {

	@Override
	protected Class<? extends Annotation> getAnnotation() {
		return EnableSharedRepositories.class;
	}

	@Override
	protected RepositoryConfigurationExtension getExtension() {
		return new SharedRepositoryConfigurationExtension();
	}

}
