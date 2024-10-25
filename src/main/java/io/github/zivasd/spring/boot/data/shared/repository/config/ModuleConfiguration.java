package io.github.zivasd.spring.boot.data.shared.repository.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

@AutoConfiguration("io.github.zivasd.spring.boot.data.shared.repository.config.JpaBeansBuilder")
public class ModuleConfiguration implements ApplicationContextAware {
	private static ApplicationContext applicationContext;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		ModuleConfiguration.applicationContext = applicationContext;
	}

	@SuppressWarnings("unchecked")
	public static <T> T autowire(Class<T> clazz) {
		Object object = ModuleConfiguration.applicationContext.getAutowireCapableBeanFactory().autowire(clazz,
				AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR, true);
		return (T)autowire(object);
	}
	
	public static <T> T autowire(T object) {
		Assert.notNull(object, "Object must not be null!");
		ModuleConfiguration.applicationContext.getAutowireCapableBeanFactory().autowireBeanProperties(object,
				AutowireCapableBeanFactory.AUTOWIRE_NO, true);
		return object;
	}
}
