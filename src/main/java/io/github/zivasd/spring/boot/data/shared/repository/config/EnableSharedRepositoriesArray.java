package io.github.zivasd.spring.boot.data.shared.repository.config;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.RetentionPolicy;

import org.springframework.context.annotation.Import;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(SharedRepositoriesRegistrar.class)
public @interface EnableSharedRepositoriesArray {
    EnableSharedRepositories[] value();
}
