# Spring Boot Simple JPA Shared Query

## Getting started

* Support DataClass and Projection
* Support Pageable


## Integrate with your tools

Maven pom.xml

```
 <groupId>io.github.zivasd</groupId>
 <artifactId>spring-boot-data-shared</artifactId>
 <version>1.0.3</version>
```

In your spring boot project
startup

```
@Configuration
@EnableSharedRepositories(basePackages = "io.github.zivasd.sample.dao.shared", entityManagerFactoryRef = "entityManagerFactory")
public class SharedStarter {
}

```

Repository in package io.github.zivasd.sample.dao.shared
```
@Repository
@Validated
public interface MySharedRepository extends SharedRepository {
	@SharedQuery(
			value = "SELECT C1 as name FROM $TABLE$ WHERE CREATE_TIME between :startTime and :endTime",
			tableNameDecider = TableNameDecider.class)
	List<Name> findObjects(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
```
