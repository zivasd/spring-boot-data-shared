package io.github.zivasd.spring.boot.data.shared.repository;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.data.annotation.QueryAnnotation;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@QueryAnnotation
@Documented
public @interface SharedQuery {
	String value() default "";
	Class<? extends TableNameDecider> tableNameDecider() default TableNameDecider.NoOperator.class;
	String countQuery() default "";
	boolean nativeQuery() default true;
	
}
