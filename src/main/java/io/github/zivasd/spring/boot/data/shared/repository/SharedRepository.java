package io.github.zivasd.spring.boot.data.shared.repository;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

@NoRepositoryBean
public interface SharedRepository extends Repository<Void, Void> {

}
