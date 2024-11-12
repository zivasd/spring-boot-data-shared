package io.github.zivasd.spring.boot.data.shared.repository.config;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotation.Adapt;
import org.springframework.core.annotation.MergedAnnotationSelectors;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

public class SharedRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {
	private ResourceLoader resourceLoader;

	@Override
	public void setResourceLoader(@NonNull ResourceLoader resourceLoader) {
		super.setResourceLoader(resourceLoader);
		this.resourceLoader = resourceLoader;
	}

	@Override
	protected Class<? extends Annotation> getAnnotation() {
		return EnableSharedRepositories.class;
	}

	@Override
	protected RepositoryConfigurationExtension getExtension() {
		return new SharedRepositoryConfigurationExtension();
	}

	@Override
	public void registerBeanDefinitions(@NonNull AnnotationMetadata metadata, @NonNull BeanDefinitionRegistry registry,
			@NonNull BeanNameGenerator generator) {
		Assert.notNull(metadata, "AnnotationMetadata must not be null");
		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
		Assert.notNull(resourceLoader, "ResourceLoader must not be null");

		if (metadata.getAnnotationAttributes(getAnnotation().getName()) != null) {
			super.registerBeanDefinitions(metadata, registry, generator);
		} else if (metadata.getAnnotationAttributes(EnableSharedRepositoriesArray.class.getName()) != null) {
			registerMultiple(metadata, registry, generator);
		}
	}

	private void registerMultiple(AnnotationMetadata metadata, BeanDefinitionRegistry registry,
			BeanNameGenerator generator) {
		MergedAnnotation<Annotation> annotation = metadata.getAnnotations().get(
				EnableSharedRepositoriesArray.class.getName(),
				null, MergedAnnotationSelectors.firstDirectlyDeclared());

		MergedAnnotation<EnableSharedRepositories>[] values = annotation.getAnnotationArray("value",
				EnableSharedRepositories.class);

		for (MergedAnnotation<EnableSharedRepositories> value : values) {
			super.registerBeanDefinitions(createAnnotationMetadataDelegate(metadata, value), registry, generator);
		}
	}

	private AnnotationMetadata createAnnotationMetadataDelegate(AnnotationMetadata metadata,
			MergedAnnotation<EnableSharedRepositories> annotation) {
		return (AnnotationMetadata) Proxy.newProxyInstance(metadata.getClass().getClassLoader(),
				new Class[] { AnnotationMetadata.class },
				new AnnotationMetadataDelegate(metadata, annotation));
	}

	static class AnnotationMetadataDelegate implements InvocationHandler {

		private final AnnotationMetadata instance;
		private final MergedAnnotation<EnableSharedRepositories> annotation;

		public AnnotationMetadataDelegate(AnnotationMetadata instance,
				MergedAnnotation<EnableSharedRepositories> annotation) {
			this.instance = instance;
			this.annotation = annotation;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if ("getAnnotationAttributes".equals(method.getName())
					|| "getAllAnnotationAttributes".equals(method.getName())) {
				return annotation.asAnnotationAttributes(Adapt.ANNOTATION_TO_MAP);
			}
			return method.invoke(instance, args);
		}
	}
}
