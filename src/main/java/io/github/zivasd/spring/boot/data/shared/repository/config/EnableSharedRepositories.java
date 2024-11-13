package io.github.zivasd.spring.boot.data.shared.repository.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Import;
import org.springframework.data.repository.config.DefaultRepositoryBaseClass;

import io.github.zivasd.spring.boot.data.shared.repository.support.SharedRepositoryFactoryBean;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Repeatable(EnableSharedRepositoriesArray.class)
@Import(SharedRepositoriesRegistrar.class)
public @interface EnableSharedRepositories {
	String[] value() default {};

	String[] basePackages() default {};

	Class<?>[] basePackageClasses() default {};

	Filter[] includeFilters() default {};

	Filter[] excludeFilters() default {};

	String repositoryImplementationPostfix() default "Impl";

	String namedQueriesLocation() default "";

	Class<?> repositoryBaseClass() default DefaultRepositoryBaseClass.class;

	Class<?> repositoryFactoryBeanClass() default SharedRepositoryFactoryBean.class;

	boolean considerNestedRepositories() default false;

	String entityManagerFactoryRef() default "entityManagerFactory";

	String transactionManagerRef() default "transactionManager";

	boolean enableDefaultTransactions() default true;

	char escapeCharacter() default '\\';
}
